package io.jbnu.mid;

import com.badlogic.gdx.math.Rectangle;

// 정해진 범위를 왔다갔다 하는 적
public class Enemy {
    public Rectangle bounds;
    public boolean alive = true;
    private float minX, maxX;
    private float speed = 100;

    public Enemy(float x, float y, float minX, float maxX) {
        bounds = new Rectangle(x, y, 44, 31);
        this.minX = minX;
        this.maxX = maxX;
    }

    public void update(float dt) {
        if (!alive) return;
        bounds.x += speed * dt;
        // 범위 끝에 닿으면 방향을 바꾼다
        if (bounds.x < minX) {
            bounds.x = minX;
            speed = -speed;
        } else if (bounds.x > maxX) {
            bounds.x = maxX;
            speed = -speed;
        }
    }
}
