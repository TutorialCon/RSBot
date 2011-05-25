package org.rsbot.script.internal;

import java.util.Random;
import org.rsbot.bot.Bot;

public class BreakHandler
{
  private final Random random = new Random();
  private long nextBreak;
  private long breakEnd;
  private int breakMin;
  private int breakMax;
  private int playMin;
  private int playMax;
  private long ticks = 0;
  private Bot bot;
  private boolean checked = false;
  private boolean result = false;

  public BreakHandler(Bot paramBot) {
    this.bot = paramBot;
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
    return /*(this.ticks < System.currentTimeMillis()) && */(this.nextBreak > 0L) && (this.nextBreak < System.currentTimeMillis()) && (this.breakEnd > System.currentTimeMillis()) && (can());
  }

  private boolean can()
  {
    if (this.checked) {
      return this.result;
    }
    this.checked = true;
    this.result = this.bot.getScriptHandler().onBreak();
    return this.result;
  }

  public void tick()
  {
    if (this.checked) {
      this.checked = false;
      this.bot.getScriptHandler().onBreakResume();
    }
    if (this.nextBreak < System.currentTimeMillis() && this.breakEnd < System.currentTimeMillis()) {
      //this.ticks = System.currentTimeMillis() + 60000;
      resetTimers();
    }
  }

  public long getBreakTime()
  {
    return this.breakEnd - System.currentTimeMillis();
  }

  private int random(int paramInt1, int paramInt2) {
    int i = Math.abs(paramInt2 - paramInt1);
    return Math.min(paramInt1, paramInt2) + (i == 0 ? 0 : this.random.nextInt(i));
  }
}