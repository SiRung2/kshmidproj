package io.jbnu.mid;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

// 레벨 하나에 들어가는 블록, 적, 발판, 점프대, 깃발 등
public class Level {
    public Array<Rectangle> blocks = new Array<>();
    public Array<Enemy> enemies = new Array<>();
    public Array<MovingPlatform> platforms = new Array<>();
    public Array<Rectangle> jumpPads = new Array<>();
    public Array<CrumbleBlock> crumbles = new Array<>();
    public Array<Rectangle> spikes = new Array<>();
    public Array<Rectangle> redBlocks = new Array<>();  // 빨강/파랑 블록은 번갈아 켜짐
    public Array<Rectangle> blueBlocks = new Array<>();
    public boolean hasLava;
    public Rectangle key;  // 없으면 null
    public Rectangle door; // 열쇠를 먹어야 열리는 문
    public Rectangle flag;
    public float width;
    public float height = 600;
    public Color bgColor;
    public String groundTop;    // 바닥 윗면 타일
    public String groundCenter; // 바닥 속 타일
    public String name;
    public String music;
    public String tip;

    public static Level create(int number) {
        if (number == 1) return level1();
        if (number == 2) return level2();
        return level3();
    }

    // 레벨 1: 짧은 맵. 알맞은 자리에서 죽어야 몸을 발판으로 쓸 수 있음
    // 퍼즐 1: 벽 바로 앞 가시에서 죽기 -> 몸을 밟고 벽 위로
    // 퍼즐 2: 구덩이에 떨어지면 몸이 안 남음 -> 가운데 가시 기둥 위에서 죽어야 다리가 생김
    // 퍼즐 3: 적에게 벽 바로 앞에서 죽기 -> 몸을 밟고 깃발로
    private static Level level1() {
        Level level = new Level();
        level.name = "Grass Field";
        level.music = "level1.wav";
        level.tip = "Your body stays where you die. Die in the right place!";
        level.width = 1600;
        level.bgColor = new Color(0.4f, 0.65f, 0.9f, 1);
        level.groundTop = "grassTop.png";
        level.groundCenter = "grassCenter.png";

        // 퍼즐 1: 점프로는 닿지 않는 벽과 그 앞의 가시
        level.blocks.add(new Rectangle(0, 0, 520, 60));
        level.spikes.add(new Rectangle(472, 60, 48, 20));
        level.blocks.add(new Rectangle(520, 0, 160, 210));

        // 퍼즐 2: 바닥 없는 구덩이 가운데의 가시 기둥
        level.blocks.add(new Rectangle(840, 0, 96, 150));
        level.spikes.add(new Rectangle(840, 150, 96, 20));

        // 퍼즐 3: 깃발이 있는 높은 벽 앞을 지키는 적
        level.blocks.add(new Rectangle(1080, 0, 520, 60));
        level.blocks.add(new Rectangle(1400, 60, 200, 150));
        level.enemies.add(new Enemy(1250, 60, 1200, 1352));

        level.flag = new Rectangle(1520, 210, 60, 64);
        return level;
    }

    // 레벨 2: 빨강/파랑 블록이 번갈아 켜지는 다리
    private static Level level2() {
        Level level = new Level();
        level.name = "lv2";
        level.music = "level2.wav";
        level.tip = "Red and blue blocks switch. Watch the timing!";
        level.width = 3000;
        level.bgColor = new Color(0.85f, 0.5f, 0.35f, 1);
        level.groundTop = "sandTop.png";
        level.groundCenter = "sandCenter.png";

        level.blocks.add(new Rectangle(0, 0, 500, 60));
        level.blocks.add(new Rectangle(1000, 0, 400, 60));
        level.blocks.add(new Rectangle(1420, 300, 280, 24));
        level.blocks.add(new Rectangle(2250, 0, 200, 60));
        level.blocks.add(new Rectangle(2750, 0, 250, 60));

        // 땅 높이의 빨강/파랑 블록
        level.redBlocks.add(new Rectangle(160, 60, 80, 24));
        level.blueBlocks.add(new Rectangle(280, 120, 80, 24));

        // 높은 곳의 빨강/파랑 다리
        level.redBlocks.add(new Rectangle(1760, 300, 80, 24));
        level.blueBlocks.add(new Rectangle(1880, 300, 80, 24));
        level.redBlocks.add(new Rectangle(2000, 300, 80, 24));
        level.blueBlocks.add(new Rectangle(2120, 300, 80, 24));

        level.platforms.add(new MovingPlatform(510, 40, 120, 510, 880));
        level.jumpPads.add(new Rectangle(1320, 60, 40, 16));

        level.crumbles.add(new CrumbleBlock(2450, 0, 96, 60));
        level.crumbles.add(new CrumbleBlock(2550, 0, 96, 60));
        level.crumbles.add(new CrumbleBlock(2650, 0, 96, 60));

        level.enemies.add(new Enemy(1100, 60, 1050, 1260));
        level.enemies.add(new Enemy(2300, 60, 2260, 2400));

        level.flag = new Rectangle(2900, 60, 60, 64);
        return level;
    }

    // 레벨 3: 열쇠를 먹으면 아래에서 용암이 차오름. 위로 도망쳐야 함
    private static Level level3() {
        Level level = new Level();
        level.name = "lv3";
        level.music = "level3.wav";
        level.tip = "Get the key to open the wall. Hurry before the lava rises!";
        level.width = 2800;
        level.height = 1100;
        level.hasLava = true;
        level.bgColor = new Color(0.15f, 0.12f, 0.3f, 1);
        level.groundTop = "stoneTop.png";
        level.groundCenter = "stoneCenter.png";

        level.blocks.add(new Rectangle(0, 0, 700, 60));
        level.blocks.add(new Rectangle(800, 360, 300, 24));
        level.blocks.add(new Rectangle(1510, 360, 290, 24));
        level.blocks.add(new Rectangle(1900, 700, 300, 24));
        level.blocks.add(new Rectangle(2300, 780, 150, 24));
        level.blocks.add(new Rectangle(2550, 840, 250, 24));

        MovingPlatform platform = new MovingPlatform(1110, 364, 110, 1110, 1400);
        platform.speed = 110; // 레벨 3은 조금 더 빠르게
        level.platforms.add(platform);

        level.jumpPads.add(new Rectangle(650, 60, 40, 16));
        level.jumpPads.add(new Rectangle(1740, 384, 40, 16));

        level.crumbles.add(new CrumbleBlock(216, 122, 48, 48));
        level.crumbles.add(new CrumbleBlock(366, 222, 48, 48));
        level.blocks.add(new Rectangle(480, 350, 80, 20));
        level.key = new Rectangle(500, 370, 40, 25);
        level.door = new Rectangle(600, 60, 40, 400);

        level.enemies.add(new Enemy(400, 60, 250, 550));
        level.enemies.add(new Enemy(900, 384, 830, 1056));
        level.enemies.add(new Enemy(2600, 864, 2560, 2680));

        level.flag = new Rectangle(2740, 864, 60, 64);
        return level;
    }
}
