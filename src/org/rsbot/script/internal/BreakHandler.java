package org.rsbot.script.internal;

import org.rsbot.bot.Bot;

import java.util.Random;

public class BreakHandler {

	private final Random random = new Random();

	private long nextBreak;
	private long breakEnd;
	private int breakMin;
	private int breakMax;
	private int playMin;
	private int playMax;
	private int ticks = 0;
	private final Bot bot;
	private boolean checked = false;
	private boolean result = false;

	public BreakHandler(final Bot bot) {
		this.bot = bot;
		this.playMin = 20;
		this.playMax = 120;
		this.breakMin = 2;
		this.breakMax = 40;
	}

	public void setBreakFor(int min, int max){
		if(max < min) return;
		this.breakMin = (min < 1) ? 1 : min;
		this.breakMax = (max < 1) ? 1 : max;
	  }
	  
	  public void setPlayTime(int min, int max){
		if(max < min) return;
		this.playMin = (min < 1) ? 1 : min;
		this.playMax = (max < 1) ? 1 : max;
	  }
	  
	  public void resetTimers(){
		int i = random(this.playMin, this.playMax) * 60000 + random(0,59999);
		int j = random(this.breakMin, this.breakMax) * 60000 + random(0,59999);
		this.nextBreak = (System.currentTimeMillis() + i);
		this.breakEnd = (nextBreak + j);
	  }
	  
	  public int getBreakMin(){return (breakMin <= 0 ? 1 : breakMin);}
	  public int getBreakMax(){return (breakMax <= 0 ? 1 : breakMax);}
	  public int getPlayMin(){return (playMin <= 0 ? 1 : playMin);}
	  public int getPlayMax(){return (playMax <= 0 ? 1 : playMax);}

	
	public boolean isBreaking() {
		return nextBreak > 0 && nextBreak < System.currentTimeMillis()
				&& breakEnd > System.currentTimeMillis() && can();
	}

	private boolean can() {
		if (checked) {
			return result;
		} else {
			checked = true;
			result = bot.getScriptHandler().onBreak();
			return result;
		}
	}

	public void tick() {
		if (checked) {
			checked = false;
			bot.getScriptHandler().onBreakResume();
		}
		if (this.nextBreak < System.currentTimeMillis() && this.breakEnd < System.currentTimeMillis()) {
		  resetTimers();
		}
	}

	public long getBreakTime() {
		return breakEnd - System.currentTimeMillis();
	}

	private int random(final int min, final int max) {
		final int n = Math.abs(max - min);
		return Math.min(min, max) + (n == 0 ? 0 : random.nextInt(n));
	}

}