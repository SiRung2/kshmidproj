package io.jbnu.mid;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

// 수업 시간에 배운 카메라 흔들림
public class CameraManager {
    private OrthographicCamera camera;

    private float shakeDuration = 0f;
    private float shakeTimer = 0f;
    private float shakeIntensity = 0f;
    private Vector2 shakeOffset = new Vector2();

    public CameraManager(OrthographicCamera camera) {
        this.camera = camera;
    }

    public void startShake(float duration, float intensity) {
        shakeDuration = duration;
        shakeTimer = 0f;
        shakeIntensity = intensity;
    }

    public void updateShake(float delta) {
        if (shakeTimer < shakeDuration) {
            shakeTimer += delta;
            // 시간이 지날수록 약하게
            float decay = Math.min(1f, shakeTimer / shakeDuration);
            float current = shakeIntensity * (1f - decay);
            shakeOffset.set(MathUtils.random(-1f, 1f) * current, MathUtils.random(-1f, 1f) * current);
        } else {
            shakeOffset.setZero();
        }
    }

    // 카메라 위치 + 흔들림
    public void setPosition(float x, float y) {
        camera.position.set(x + shakeOffset.x, y + shakeOffset.y, 0);
        camera.update();
    }
}
