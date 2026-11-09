package io.jbnu.mid;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {

    private static final float MOVE_SPEED = 260;
    private static final float JUMP_SPEED = 650;
    private static final float GRAVITY = 1700;
    private static final float PAD_SPEED = 1200;
    private static final int LAST_LEVEL = 3;
    private static final float SWITCH_TIME = 1.5f; // 빨강/파랑 블록이 바뀌는 간격
    private static final float LAVA_SPEED = 30;    // 용암이 올라오는 속도
    private static final float TILE = 48;          // 바닥 타일 한 칸 크기

    private SpriteBatch batch;
    private BitmapFont font;
    private Texture playerTexture, blockTexture, flagTexture, spikeTexture, enemyTexture, keyTexture;
    private Texture springDownTexture, springUpTexture;
    private Texture platformTexture, crateTexture, crateWarningTexture, lockTexture, brickTexture;
    private Texture groundTopTexture, groundCenterTexture; // 레벨마다 다름
    private OrthographicCamera camera;
    private FitViewport viewport;
    private CameraManager cameraManager;
    private Sound jumpSound, stompSound, dieSound, keySound, clearSound;
    private Music music;

    private Level level;
    private int levelNumber;
    private GameState state;
    private float clearTimer;
    private float restartTimer; // R 키를 누르고 있는 시간
    private float camX, camY;

    private final Rectangle player = new Rectangle(0, 0, 32, 44);
    private final Array<Rectangle> solids = new Array<>();
    private Rectangle standingOn; // 지금 밟고 있는 블록 (움직이는 발판 확인용)
    private float velocityX, velocityY;
    private boolean grounded;
    private boolean hasKey;

    private final Array<Rectangle> bodies = new Array<>(); // 죽은 자리에 남는 몸 (밟을 수 있음)
    private float switchTimer;
    private boolean redOn = true;
    private float lavaHeight;
    private Rectangle springPad;  // 방금 밟은 점프대 (잠깐 늘어난 모습으로 그림)
    private float springTimer;


    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.5f);
        playerTexture = new Texture(Gdx.files.internal("player.png"));
        blockTexture = new Texture(Gdx.files.internal("block.png"));
        flagTexture = new Texture(Gdx.files.internal("flag.png"));
        spikeTexture = new Texture(Gdx.files.internal("spike.png"));
        enemyTexture = new Texture(Gdx.files.internal("enemy.png"));
        keyTexture = new Texture(Gdx.files.internal("key.png"));
        springDownTexture = new Texture(Gdx.files.internal("springDown.png"));
        springUpTexture = new Texture(Gdx.files.internal("springUp.png"));
        platformTexture = new Texture(Gdx.files.internal("platform.png"));
        crateTexture = new Texture(Gdx.files.internal("crate.png"));
        crateWarningTexture = new Texture(Gdx.files.internal("crateWarning.png"));
        lockTexture = new Texture(Gdx.files.internal("lock.png"));
        brickTexture = new Texture(Gdx.files.internal("brick.png"));
        lockTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        brickTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        viewport = new FitViewport(800, 600, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        cameraManager = new CameraManager(camera);

        jumpSound = Gdx.audio.newSound(Gdx.files.internal("jump.wav"));
        stompSound = Gdx.audio.newSound(Gdx.files.internal("stomp.wav"));
        dieSound = Gdx.audio.newSound(Gdx.files.internal("die.wav"));
        keySound = Gdx.audio.newSound(Gdx.files.internal("key.wav"));
        clearSound = Gdx.audio.newSound(Gdx.files.internal("clear.wav"));

        loadLevel(1);
    }

    private void loadLevel(int number) {
        levelNumber = number;
        level = Level.create(number);
        bodies.clear();
        state = GameState.PLAYING;
        resetPlayer();

        // 레벨마다 바닥 타일이 다름 (반복해서 깔 수 있게 Repeat)
        if (groundTopTexture != null) {
            groundTopTexture.dispose();
            groundCenterTexture.dispose();
        }
        groundTopTexture = new Texture(Gdx.files.internal(level.groundTop));
        groundCenterTexture = new Texture(Gdx.files.internal(level.groundCenter));
        groundTopTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        groundCenterTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        // 레벨마다 배경음이 다름
        if (music != null) music.dispose();
        music = Gdx.audio.newMusic(Gdx.files.internal(level.music));
        music.setLooping(true);
        music.setVolume(0.2f);
        music.play();
    }

    private void resetPlayer() {
        player.setPosition(80, 60);
        velocityX = 0;
        velocityY = 0;
        grounded = false;
        standingOn = null;
        hasKey = false;
        switchTimer = 0;
        redOn = true;
        lavaHeight = 0;
        springTimer = 0;
        updateCamera();
    }

    // 죽으면 레벨을 처음부터 다시 (적도 다시 살아남)
    // 대신 죽은 자리에 몸이 블록으로 남아서 다음 시도에 밟을 수 있다
    private void die() {
        if (player.y > 0) bodies.add(new Rectangle(player.x, player.y, player.width, player.height));
        dieSound.play(0.3f);
        cameraManager.startShake(0.3f, 8);
        level = Level.create(levelNumber);
        resetPlayer();
    }


    @Override
    public void render() {
        // 큰 프레임 시간으로 인해 한 번에 너무 멀리 이동하는 것을 제한합니다.
        float dt = Math.min(Gdx.graphics.getDeltaTime(), 1f / 30f);
        input();
        update(dt);
        draw();
    }

    private void input() {
        // 모든 레벨 클리어 후 ENTER 로 처음부터
        if (state == GameState.GAME_CLEAR) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) loadLevel(1);
            return;
        }

        // P 또는 ESC 로 일시정지 / 재개
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (state == GameState.PLAYING) {
                state = GameState.PAUSED;
                music.pause();
            } else if (state == GameState.PAUSED) {
                state = GameState.PLAYING;
                music.play();
            }
        }
    }

    private void update(float dt) {
        if (dt <= 0) return;
        cameraManager.updateShake(dt);

        if (state == GameState.PAUSED || state == GameState.GAME_CLEAR) {
            cameraManager.setPosition(camX, camY);
            return;
        }

        // 깃발에 닿으면 화면을 위로 올리고 2초 뒤 다음 레벨
        if (state == GameState.LEVEL_CLEAR) {
            camY += 50f * dt;
            cameraManager.setPosition(camX, camY);
            clearTimer += dt;
            if (clearTimer > 2f) {
                if (levelNumber == LAST_LEVEL) state = GameState.GAME_CLEAR;
                else loadLevel(levelNumber + 1);
            }
            return;
        }

        // 0. R 키를 1초 동안 누르고 있으면 레벨 재시작 (갇혔을 때)
        if (Gdx.input.isKeyPressed(Input.Keys.R)) {
            restartTimer += dt;
            if (restartTimer > 1f) {
                restartTimer = 0;
                bodies.clear(); // 재시작하면 남은 몸도 지움
                level = Level.create(levelNumber);
                resetPlayer();
                return;
            }
        } else {
            restartTimer = 0;
        }

        // 1. 움직이는 발판과 적 이동 (발판 위에 있으면 같이 이동)
        for (MovingPlatform p : level.platforms) {
            p.update(dt);
            if (standingOn == p.bounds) player.x += p.dx;
        }
        for (Enemy e : level.enemies) e.update(dt);
        for (CrumbleBlock c : level.crumbles) c.update(dt);

        // 빨강/파랑 블록 전환
        switchTimer += dt;
        if (switchTimer > SWITCH_TIME) {
            switchTimer = 0;
            redOn = !redOn;
        }

        // 열쇠를 먹은 뒤부터 용암이 올라옴
        if (level.hasLava && hasKey) lavaHeight += LAVA_SPEED * dt;

        // 2. 좌우 이동과 지상 점프
        velocityX = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) velocityX -= MOVE_SPEED;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) velocityX += MOVE_SPEED;
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && grounded) {
            velocityY = JUMP_SPEED;
            grounded = false;
            jumpSound.play(0.3f);
        }

        // 3. 중력: 아래쪽 속도는 음수이며 최대 낙하 속도를 제한합니다.
        velocityY = Math.max(velocityY - GRAVITY * dt, -900);

        // 4. X축과 Y축을 나누어 이동하고 충돌을 보정합니다.
        //    (블록 + 움직이는 발판 + 무너지는 블록 + 닫힌 문 + 죽은 몸 + 켜진 색 블록)
        solids.clear();
        solids.addAll(level.blocks);
        solids.addAll(bodies);
        solids.addAll(redOn ? level.redBlocks : level.blueBlocks);
        for (MovingPlatform p : level.platforms) solids.add(p.bounds);
        for (CrumbleBlock c : level.crumbles) {
            if (c.solid) solids.add(c.bounds);
        }
        if (level.door != null && !hasKey) solids.add(level.door);
        moveX(dt);
        moveY(dt);

        // 무너지는 블록을 밟았으면 무너지기 시작
        for (CrumbleBlock c : level.crumbles) {
            if (standingOn == c.bounds) c.step();
        }

        // 5. 점프대: 밟으면 높이 튀어오름
        if (springTimer > 0) springTimer -= dt;
        for (Rectangle pad : level.jumpPads) {
            if (player.overlaps(pad) && velocityY <= 0) {
                velocityY = PAD_SPEED;
                grounded = false;
                springPad = pad;
                springTimer = 0.3f;
                cameraManager.startShake(0.15f, 4);
            }
        }

        // 6. 적: 위에서 밟으면 처치, 옆에서 닿으면 죽음
        for (Enemy e : level.enemies) {
            if (!e.alive || !player.overlaps(e.bounds)) continue;
            if (velocityY < 0 && player.y > e.bounds.y + e.bounds.height / 2) {
                e.alive = false;
                velocityY = 450;
                stompSound.play(0.3f);
                cameraManager.startShake(0.2f, 5);
            } else {
                die();
                return;
            }
        }

        // 7. 낭떠러지, 가시, 용암에 닿으면 죽음
        if (player.y < -100) {
            die();
            return;
        }
        for (Rectangle spike : level.spikes) {
            if (player.overlaps(spike)) {
                die();
                return;
            }
        }
        if (level.hasLava && player.y < lavaHeight) {
            die();
            return;
        }

        // 8. 열쇠를 먹으면 문이 열림
        if (level.key != null && !hasKey && player.overlaps(level.key)) {
            hasKey = true;
            keySound.play(0.3f);
        }

        // 9. 깃발에 닿으면 레벨 클리어
        if (player.overlaps(level.flag)) {
            state = GameState.LEVEL_CLEAR;
            clearTimer = 0;
            music.stop();
            clearSound.play(0.3f);
        }

        // 10. 카메라가 플레이어를 따라감
        updateCamera();
    }

    private void updateCamera() {
        camX = MathUtils.clamp(player.x + player.width / 2, 400, level.width - 400);
        camY = MathUtils.clamp(player.y + player.height / 2, 300, level.height - 300);
        cameraManager.setPosition(camX, camY);
    }

    private void moveX(float dt) {
        player.x += velocityX * dt;
        for (Rectangle block : solids) {
            if (!player.overlaps(block)) continue;
            if (velocityX > 0) player.x = block.x - player.width;
            else if (velocityX < 0) player.x = block.x + block.width;
        }
        player.x = MathUtils.clamp(player.x, 0, level.width - player.width);
    }

    private void moveY(float dt) {
        float dy = velocityY * dt;
        player.y += dy;
        grounded = false;
        standingOn = null;

        for (Rectangle block : solids) {
            if (!player.overlaps(block)) continue;
            if (dy < 0) { // 내려오던 중: 블록 위에 착지
                player.y = block.y + block.height;
                velocityY = 0;
                grounded = true;
                standingOn = block;
            } else if (dy > 0) { // 올라가던 중: 블록 아래에 머리 충돌
                player.y = block.y - player.height;
                velocityY = 0;
            }
        }
    }


    private void draw() {
        ScreenUtils.clear(level.bgColor);

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // 바닥: 늘리지 않고 타일을 반복해서 깔기
        batch.setColor(Color.WHITE);
        for (Rectangle block : level.blocks) drawGround(block);
        // 움직이는 발판: 나무 판자
        for (MovingPlatform p : level.platforms) {
            batch.draw(platformTexture, p.bounds.x, p.bounds.y, p.bounds.width, p.bounds.height);
        }
        // 점프대: 평소엔 눌린 스프링, 밟은 직후엔 늘어난 스프링
        batch.setColor(Color.WHITE);
        for (Rectangle pad : level.jumpPads) {
            if (pad == springPad && springTimer > 0) batch.draw(springUpTexture, pad.x, pad.y, pad.width, 29);
            else batch.draw(springDownTexture, pad.x, pad.y, pad.width, 21);
        }
        // 무너지는 블록: 나무 상자, 밟으면 느낌표 상자로 바뀜
        for (CrumbleBlock c : level.crumbles) {
            if (!c.solid) continue;
            Texture t = c.stepped ? crateWarningTexture : crateTexture;
            batch.draw(t, c.bounds.x, c.bounds.y, c.bounds.width, c.bounds.height);
        }
        // 빨강/파랑 블록: 꺼진 쪽은 흐리게, 바뀌기 직전에는 깜빡임
        boolean blink = switchTimer > SWITCH_TIME - 0.4f && (int) (switchTimer * 10) % 2 == 0;
        batch.setColor(1f, 0.4f, 0.4f, redOn && !blink ? 1f : 0.25f);
        for (Rectangle b : level.redBlocks) drawTiled(brickTexture, b, 40);
        batch.setColor(0.4f, 0.6f, 1f, !redOn && !blink ? 1f : 0.25f);
        for (Rectangle b : level.blueBlocks) drawTiled(brickTexture, b, 40);

        batch.setColor(Color.WHITE);
        for (Rectangle spike : level.spikes) {
            // 가시 한 개 = 24px. 끝부분이 블록 밖으로 나가지 않게 마지막 칸은 잘라서 그림
            for (float x = spike.x; x < spike.x + spike.width; x += 24) {
                float w = Math.min(24, spike.x + spike.width - x);
                batch.draw(spikeTexture, x, spike.y, w, spike.height,
                    0, 0, (int) (spikeTexture.getWidth() * w / 24), spikeTexture.getHeight(), false, false);
            }
        }

        // 죽은 몸은 회색 반투명 캐릭터로
        batch.setColor(0.5f, 0.5f, 0.5f, 0.7f);
        for (Rectangle body : bodies) batch.draw(playerTexture, body.x, body.y, body.width, body.height);

        if (level.door != null && !hasKey) {
            // 닫힌 문: 노란 자물쇠 블록을 쌓아서 (노란 열쇠로 열림)
            batch.setColor(Color.WHITE);
            drawTiled(lockTexture, level.door, 40);
        }
        if (level.key != null && !hasKey) {
            batch.setColor(Color.WHITE);
            batch.draw(keyTexture, level.key.x, level.key.y, level.key.width, level.key.height);
        }
        batch.setColor(Color.WHITE);
        for (Enemy e : level.enemies) {
            if (e.alive) batch.draw(enemyTexture, e.bounds.x, e.bounds.y, e.bounds.width, e.bounds.height);
        }
        batch.setColor(Color.WHITE);

        batch.draw(flagTexture, level.flag.x, level.flag.y, level.flag.width, level.flag.height);
        batch.draw(playerTexture, player.x, player.y, player.width, player.height);

        if (level.hasLava) {
            batch.setColor(1f, 0.35f, 0.05f, 0.85f);
            batch.draw(blockTexture, 0, -100, level.width, lavaHeight + 100);
            batch.setColor(Color.WHITE);
        }

        // 글자 (카메라 기준 위치)
        float left = camera.position.x - 390;
        float top = camera.position.y + 290;
        font.draw(batch, "LEVEL " + levelNumber + " / " + LAST_LEVEL + "  " + level.name, left, top);
        font.draw(batch, "P: Pause", left, top - 30);
        font.draw(batch, "R (hold): Restart", left, top - 60);
        font.draw(batch, level.tip, left, top - 90);
        if (level.key != null) font.draw(batch, hasKey ? "KEY: O" : "KEY: X", left, top - 120);

        float centerX = camera.position.x - 400;
        float centerY = camera.position.y + 40;
        if (state == GameState.PAUSED) {
            font.draw(batch, "PAUSED\nPress P to resume", centerX, centerY, 800, Align.center, false);
        } else if (state == GameState.LEVEL_CLEAR) {
            font.draw(batch, "LEVEL CLEAR!", centerX, centerY, 800, Align.center, false);
        } else if (state == GameState.GAME_CLEAR) {
            font.draw(batch, "ALL LEVELS CLEAR!\nPress ENTER to restart", centerX, centerY, 800, Align.center, false);
        }

        batch.end();
    }


    // 타일 하나를 블록 크기만큼 반복해서 그림
    private void drawTiled(Texture texture, Rectangle b, float tile) {
        batch.draw(texture, b.x, b.y, b.width, b.height, 0, b.height / tile, b.width / tile, 0);
    }

    // 블록 맨 위 한 칸은 윗면 타일, 나머지는 속 타일
    // (u, v 를 블록 크기 / 타일 크기로 주면 그 횟수만큼 반복해서 그려진다)
    private void drawGround(Rectangle b) {
        float top = Math.min(TILE, b.height);
        float rest = b.height - top;
        batch.draw(groundTopTexture, b.x, b.y + rest, b.width, top, 0, top / TILE, b.width / TILE, 0);
        if (rest > 0) {
            batch.draw(groundCenterTexture, b.x, b.y, b.width, rest, 0, rest / TILE, b.width / TILE, 0);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        playerTexture.dispose();
        blockTexture.dispose();
        flagTexture.dispose();
        spikeTexture.dispose();
        enemyTexture.dispose();
        keyTexture.dispose();
        springDownTexture.dispose();
        springUpTexture.dispose();
        platformTexture.dispose();
        crateTexture.dispose();
        crateWarningTexture.dispose();
        lockTexture.dispose();
        brickTexture.dispose();
        groundTopTexture.dispose();
        groundCenterTexture.dispose();
        jumpSound.dispose();
        stompSound.dispose();
        dieSound.dispose();
        keySound.dispose();
        clearSound.dispose();
        music.dispose();
    }
}
