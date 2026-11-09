package io.jbnu.mid;

import com.badlogic.gdx.math.Rectangle;

// 밟으면 잠시 뒤 사라지고, 몇 초 뒤 다시 생기는 블록
public class CrumbleBlock {
    private static final float BREAK_TIME = 0.5f;
    private static final float RESPAWN_TIME = 3f;

    public Rectangle bounds;
    public boolean solid = true;
    public boolean stepped;
    private float timer;

    public CrumbleBlock(float x, float y, float width, float height) {
        bounds = new Rectangle(x, y, width, height);
    }

    // 플레이어가 위에 올라왔을 때
    public void step() {
        if (solid && !stepped) {
            stepped = true;
            timer = 0;
        }
    }

    public void update(float dt) {
        if (stepped) {
            timer += dt;
            if (timer > BREAK_TIME) { // 사라짐
                solid = false;
                stepped = false;
                timer = 0;
            }
        } else if (!solid) {
            timer += dt;
            if (timer > RESPAWN_TIME) { // 다시 생김
                solid = true;
                timer = 0;
            }
        }
    }
}
