// EngineInterface.java

package procsynth.shine3.papplet;

import java.util.List;
import java.util.Map;

import processing.core.PApplet;
import processing.core.PVector;
import procsynth.shine3.Shine3;
import procsynth.shine3.engine.Block;
import procsynth.shine3.engine.Engine;
import procsynth.shine3.engine.Input;
import procsynth.shine3.engine.Output;
import procsynth.shine3.utils.Pair;

/**
 * PApplet graphic interface for Shine3 engine. The EngineInterface is a
 * Processing sketch that render the state of an engine and its blocks. It is
 * also used to interact with it using the mouse and keyboard.
 *
 * @author procsynth - Antoine Pintout
 * @since v0.0.1
 */
public class UI extends PApplet {

	/** Stores the arguments passed the Processing sketch */
	private static final String[] PAPPLET_ARGS = new String[] { "" };

	/** The reference of the BlockEngine that is interfaced. */
	private Engine engine;
	/** The root drawable */
	private Drawable root;

	/**
	 * This creates a PApplet sketch that will render a representation of the
	 * Engine.
	 *
	 * @see Engine
	 * 
	 */
	public UI(String[] args) {
		PApplet.runSketch(concat(args, PAPPLET_ARGS), this);

		// generate a map for the first time
		List<Block> blocks = Shine3.engine.getBlocks();
		Map<Pair<Integer, Integer>, Block> map = Mapgen.getOrthoMap(blocks);
	}

	/** Settings of the PApplet. */
	@Override
	public void settings() {
		size(1920, 1080, PApplet.P3D);
		pixelDensity(displayDensity());
		smooth(4);
	}

	/** Setup of the PApplet. Set renderer properties. */
	@Override
	public void setup() {
		frameRate(160);
		surface.setResizable(true);
		surface.setTitle("Shine 3");
		hint(PApplet.DISABLE_DEPTH_TEST);

		if(pixelDensity == 2){
			textFont(loadFont("Raleway-Regular-36.vlw"));
		}else{
			textFont(loadFont("Raleway-Regular-18.vlw"));
		}

		root = new RootDrawable(g);
	}

	/**
	 * Main loop of the PApplet.
	 */
	@Override
	public void draw() {
		background(0x00, 0x10, 0x21);
		pushMatrix();

		List<Block> blocks = Shine3.engine.getBlocks();

		// // setup 3D camera

		// tweak frustrum near and far plane to avoid clipping;
		ortho(-width / 2, width / 2, -height / 2, height / 2, -height, 2 * height);
		camera(Math.min(width, height) / 1.8f, Math.min(width, height) / 2.0f,
			(Math.min(width, height) / 2.0f) / tan(PI * 28.0f / 180.0f), 0, 0, 0, 0, 0, -1);

		Unproject.captureViewMatrix(this);
		PVector mouse = Unproject.unproject(mouseX, mouseY, 0);

		//

		draw3DMap(blocks, mouse);

		drawCursor(mouse);
		popMatrix();

		drawStatus();
		drawMinimap(blocks);
		drawBlockMenu();

	}

	private void draw3DMap(List<Block> blocks, PVector mouse) {

		Map<Block, Pair<Integer, Integer>> map = Mapgen.getOrthoMapByBlock(blocks);

		rectMode(CORNER);
		fill(Palette.RED);
		noFill();

		for (Block b : blocks) {
			Pair<Integer, Integer> coords = map.get(b);
			pushMatrix();

			if (coords.A() * -170 - 120 / 2 < mouse.x && mouse.x < coords.A() * -170 + 120 / 2
					&& coords.B() * 220 - 170 / 2 < mouse.y && mouse.y < coords.B() * 220 + 170 / 2) {
				stroke(Palette.RED);
			} else {
				stroke(Palette.ORANGE);
			}
			translate(coords.A() * -170, coords.B() * 220, 15);
			rectMode(CENTER);
			rect(0, 0, 120, 170);
			translate(0, 0, -30);
			stroke(Palette.ORANGE_DARK);
			rect(0, 0, 120, 170);
			line(-120/2, -170/2, 0, -120/2, -170/2, 30);
			line(-120/2, 170/2, 0, -120/2, 170/2, 30);
			line(120/2, -170/2, 0, 120/2, -170/2, 30);
			line(120/2, 170/2, 0, 120/2, 170/2, 30);
			rotateZ(-HALF_PI);
			translate(-170 / 2, -120 / 2, 30);
			text(b.getDisplayName(), 0, 0);

			int offsetOut = 170 - 20;
			for (Output o : b.getOutputs().values()) {
				stroke(Palette.YELLOW);
				ellipse(offsetOut, 120 - 9, 10, 10);
				offsetOut -= 20;
			}
			int offsetIn = 170 - 20;
			for (Input o : b.getInputs().values()) {
				stroke(Palette.GREEN);
				ellipse(offsetIn, 9, 10, 10);
				offsetOut -= 20;
			}

			popMatrix();
		}
	}

	private void drawStatus() {
		textSize(18);

		fill(Palette.RED);
		textAlign(PApplet.LEFT);
		text(Math.round(frameRate), 12, height - 12);
		fill(Palette.GREEN);
		text(Math.round(Shine3.engine.getTickRate()), 40, height - 12);

		textAlign(PApplet.RIGHT);
		text(Shine3.VERSION, width - 12, height - 12);

		fill(Palette.BLUE);
		textAlign(PApplet.CENTER);
		text("shine 3 / procsynth", width / 2, height - 10);
	}

	private void drawCursor(PVector mouse) {

		pushMatrix();
		translate(mouse.x, mouse.y);

		stroke(Palette.RED);
		line(0, 0, 0, -20, 0, 0);
		stroke(Palette.GREEN);
		line(0, 0, 0, 0, -20, 0);
		stroke(Palette.BLUE);
		line(0, 0, 0, 0, 0, 20);

		// rotateZ(PI);
		fill(Palette.GREEN);
		textAlign(PApplet.LEFT, PApplet.BOTTOM);
	//	text(mouseX + ";" + mouseY + ";", 10, -10);
		popMatrix();
	}

	private void drawBlockMenu() {

		int offset = 40;

		stroke(Palette.RED);
		for (Class<?> c : Shine3.engine.getAvailableBlocks()) {
			fill(Palette.RED);
			text(c.getSimpleName().toLowerCase(), 30, offset);
			offset += 25;
		}
	}

	private void drawMinimap(List<Block> blocks) {

		Map<Pair<Integer, Integer>, Block> map = Mapgen.getOrthoMap(blocks);

		// System.out.println("min: "+ Mapgen.level +" max: "+ Mapgen.maxLevel +" width:
		// "+Mapgen.maxSpot);

		stroke(Palette.RED);
		rectMode(CENTER);
		ellipseMode(CENTER);
		textAlign(LEFT);
		fill(Palette.RED);
		noFill();

		for (int i = 0; i <= Mapgen.maxLevel; i++) {
			for (int j = 0; j <= Mapgen.maxSpot; j++) {
				if (map.containsKey(new Pair(i, j))) {
					Block b = map.get(new Pair(i, j));

					stroke(Palette.RED);
					rect(width - 80 - j * 90, 50 + (Mapgen.maxLevel - i) * 50, 80, 40);
					text(b.getDisplayName(), width - 115 - j * 90, 50 + (Mapgen.maxLevel - i) * 50);
					int oofs = 5;
					for (Output o : b.getOutputs().values()) {
						if ((o.value() instanceof Boolean) && (Boolean) o.value()) {
							stroke(Palette.YELLOW);
						} else {
							stroke(Palette.BLUE);
						}
						ellipse(width - 115 - j * 90 + oofs, 60 + (Mapgen.maxLevel - i) * 50, 10, 10);
						oofs += 15;
					}
				}
			}
		}

	}

}
