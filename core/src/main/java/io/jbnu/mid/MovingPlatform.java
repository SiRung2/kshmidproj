package io.jbnu.mid;

import com.badlogic.gdx.math.Rectangle;

// 좌우로 움직이는 발판. 위에 서 있으면 플레이어도 같이 움직인다.
public class MovingPlatform {
    public Rectangle bounds;
    public float dx; // 이번 프레임에 움직인 거리
    private float minX, maxX;
    public float speed = 90;

    public MovingPlatform(float x, float y, float width, float minX, float maxX) {
        bounds = new Rectangle(x, y, width, 20);
        this.minX = minX;
        this.maxX = maxX;
    }

    public void update(float dt) {
        float oldX = bounds.x;
        bounds.x += speed * dt;
        if (bounds.x < minX) {
            bounds.x = minX;
            speed = -speed;
        } else if (bounds.x > maxX) {
            bounds.x = maxX;
            speed = -speed;
        }
        dx = bounds.x - oldX;
    }
}
