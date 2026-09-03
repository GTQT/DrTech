package com.drppp.drtech.client.render.wings;

public interface Animator {
	void beginLand();

	void beginGlide();

	void beginIdle();

	void beginLift();

	void beginFall();

	void update();
}
