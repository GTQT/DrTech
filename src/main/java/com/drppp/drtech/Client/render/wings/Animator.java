package com.drppp.drtech.Client.render.wings;

public interface Animator {
	void beginLand();

	void beginGlide();

	void beginIdle();

	void beginLift();

	void beginFall();

	void update();
}
