package framework;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;

import game.GameLevels;
import game.KeyState;

public class LevelManager {
	ArrayList<Level> levels = new ArrayList<Level>();
	private GameLevels curLevel;
	private KeyMapping keys;
	private double scale;
	
	public LevelManager()
	{
		curLevel = GameLevels.MENU;
		keys = new KeyMapping();
		keys.setKey("Enter", 10);
		keys.setKey("Left", 37);
		keys.setKey("Right", 39);
		keys.setKey("Esc", 27);
		keys.setKey("Jump", 38);
	}
	public KeyMapping getKeyMapping()
	{
		return this.keys;
	}
	public void setLevel(GameLevels newLevel)
	{
		curLevel = newLevel;
	}
	
	public void addLevel(Level newLevel)
	{
		levels.add(newLevel);
		levels.set(newLevel.getLevel().ordinal(), newLevel);
	}
	public double getScale() {
		return scale;
	}
	public void setScale(double scale) {
		this.scale = scale;
	}
	public void update(KeyState keys, boolean mouseDown)
	{
		levels.get(curLevel.ordinal()).update(keys);
	}
	public void updateUI(Point mouseClick)
	{
		this.levels.get(curLevel.ordinal()).updateUI(mouseClick);
	}
	public void render(Graphics2D g2d, boolean debug)
	{
		this.levels.get(curLevel.ordinal()).render(g2d, debug);
	}
	public void renderUI(Graphics2D g2d, boolean debug)
	{
		this.levels.get(curLevel.ordinal()).renderUI(g2d, debug);
	}
}
