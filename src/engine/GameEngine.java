package engine;


import java.nio.file.Path;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.WindowConstants;


public class GameEngine extends JFrame
{
	
	
	//buffer for drawing off screen
	private Image raster;
	
	
	//graphics for the buffer
	private Graphics rasterGraphics;
	
	
	//this is the current x and y of the ball	
	private Image background;
	
	
	
	
	
	
	public static void main(String[] args) {
		GameEngine f = new GameEngine();
		f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		f.setSize(800,800);		
		f.setVisible(true);	
		f.setup();
		f.draw();
	}
	
	
	
	
	static class Camera
	{
		public static Camera camera = new Camera();
		private int CameraX=0, CameraY = 0;
		public int getX(){return CameraX;}
		public int getY(){return CameraY;}
		private Camera(){}
	}
	
		
	//list of all the gameobjects (other than the player)
	ArrayList<ScreenObj> gameObjects = new ArrayList<ScreenObj>();
	
	GameState state = GameState.RUNNING;
	
	boolean CLICK = false;
	
	private Vector2D clickPosition;
	
	
	public void setup()
	{
		//setup buffered graphics
		raster = this.createImage(800, 800);
		//raster = new BufferedImage(BufferedImage.TYPE_4BYTE_ABGR,500,500);
		rasterGraphics = raster.getGraphics();
		
		
		//Just handle pause menu key listener - anonymous class
		addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent arg0) {}
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_P)
				{
					if (state == GameState.RUNNING)
						state = GameState.PAUSED;
					else
						state = GameState.RUNNING;
				}
			}
			
			
			public void keyTyped(KeyEvent e) {}			
				});
				
		
				addMouseListener(new MouseListener(){
					public void mouseClicked(MouseEvent e) {}
					public void mouseEntered(MouseEvent e) {}
					public void mouseExited(MouseEvent e) {}
					public void mousePressed(MouseEvent e) {
						CLICK = true;
						clickPosition = new Vector2D(e.getX(),e.getY());
					}
					public void mouseReleased(MouseEvent e) 
					{
						CLICK = false;
					}
				});
			
	}
	
	
	
	
	
	public void readLevel()
	{
		
		File fileObj = new File("SimplePlatformerExampleLevel.txt");
		
		
		try(Scanner file = new Scanner(fileObj);) {		
				

			while (file.hasNextLine())
			{
				
				String line = file.nextLine();
				if (line.startsWith("Block"))
				{
					String tokens[] = line.split(",");
					
					
					gameObjects.add(new Block(Integer.parseInt(tokens[1]),
							Integer.parseInt(tokens[2]),
							Integer.parseInt(tokens[3]),
							Integer.parseInt(tokens[4]),
							new Color(Integer.parseInt(tokens[5]),
									Integer.parseInt(tokens[6]),
									Integer.parseInt(tokens[7]))
							));
				}
				//TODO: Add additional level information here
			}
		
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println(fileObj.getAbsolutePath());
		}
	}
	
	
	
	
	enum GameState
	{
		RUNNING,
		PAUSED,
	}
	
	/**
	 * This is the workhorse of the program. This is where the main loop for graphics is done
	 */
	
	
	
	
	
	public void draw()
	{
		//create and add the balls to an array to use later
		PlatPlayer p = new PlatPlayer(100,100,Color.WHITE);
		this.addKeyListener(p);
		long deltaTime = 0;				
		readLevel();
		while(true)
		{
			//get the start time of the loop to use later
				long time = System.currentTimeMillis();
			
			if (state == GameState.RUNNING)
			{	
				GameState(p);
			}
			else if (state == GameState.PAUSED)
			{
				PausedState();		
			}
			
			
			//draw the scene from the buffered raster (all at once to avoid flickering)
				getGraphics().drawImage(raster,0,0,getWidth(),getHeight(),null);
			
			//use the start time minus the current time to get delta time - this will 
			//vary as your program runs to make your program run smoothly we are going 
			//to use delta time when we sleep
				deltaTime = System.currentTimeMillis() - time;	
				deltaTime = Math.max(deltaTime, 10);
				try{Thread.sleep(deltaTime);}catch(Exception e){}
				
		}
		
	}
	public void GameState(PlatPlayer p)
	{
		//draw and move the background and balls
		DrawBackground(rasterGraphics);
		
	//level
		for (ScreenObj b : gameObjects)
		{
			b.draw(rasterGraphics);
		}
		
	//player and other actors(enemies)
		p.MoveBall();
		p.Draw(rasterGraphics);
		for (ScreenObj b : gameObjects)
			if (b instanceof Collidable)
				p.checkCollision((Collidable)b);
					
	//update camera (commented out other ways of handling camera
//		int current = (int)p.Location.getX();
//		if (current - CameraX > getWidth() * 4 / 5)
//			CameraX = current - (getWidth() * 4 / 5); 
//		else if (current - CameraX < getWidth() * 1 / 5)
//			CameraX = current - (getWidth() * 1 / 5);
		Camera.camera.CameraX = (int)p.Location.getX() - getWidth()/2;
		
//		current = (int)p.Location.getY();
//		if (current - CameraY > getHeight() * 2 / 3)
//			CameraY = current - (getHeight() * 2 / 3); 
//		else if (current - CameraY < getHeight() * 1 / 3)
//			CameraY = current - (getHeight() * 1 / 3);
		Camera.camera.CameraY = (int)p.Location.getY()- getHeight()/2;
		
	}
	public void PausedState()
	{
		//example of manually creating/controlling buttons
		
		Rectangle SaveButton = new Rectangle(200,100,180,100);
		Rectangle LoadButton = new Rectangle(200,220,180,100);
		
		rasterGraphics.setColor(Color.RED);
		((Graphics2D) rasterGraphics).fill(SaveButton);
		rasterGraphics.setColor(Color.GREEN);
		rasterGraphics.setFont(new Font("Arial",Font.PLAIN,34));
		rasterGraphics.drawString("Save",220,150);
		
		rasterGraphics.setColor(Color.RED);
		((Graphics2D) rasterGraphics).fill(LoadButton);
		rasterGraphics.setColor(Color.GREEN);
		rasterGraphics.drawString("Load",220,270);
		
		if (CLICK)
		{
			java.awt.Point pt = new java.awt.Point((int)clickPosition.getX(),(int)clickPosition.getY());
			if (SaveButton.contains(pt)) 
				System.out.println("Saving game please wait");
			if (LoadButton.contains(pt))
				System.out.println("Loading game please wait");
			CLICK = false;
		}	
	}
	private void DrawBackground(Graphics g) 
	{
		g.setColor(new Color(170,180,240));
		g.fillRect(0, 0, 800, 800);
	}	
}
interface Collidable
{
	public Rectangle getCollision();
}
abstract class ScreenObj implements Serializable
{
	public Vector2D Location;
	public Vector2D Size;
	
	public abstract void draw(Graphics g);
}
class Block extends ScreenObj implements Collidable, Serializable
{
	public Color C;
	
	public Block(int X, int Y, int Xsize, int Ysize, Color c)
	{
		Location = new Vector2D(X,Y);
		Size = new Vector2D(Xsize,Ysize);
		C = c;
	}
	public Rectangle getCollision()
	{
		return new Rectangle((int)Location.getX(), (int)Location.getY(), (int)Size.getX(), (int)Size.getY());
	}
	public void draw(Graphics g)
	{
		g.setColor(C);
		g.fillRect((int)Location.getX() - GameEngine.Camera.camera.getX(), (int)Location.getY() - GameEngine.Camera.camera.getY(), (int)Size.getX(), (int)Size.getY());
		//Rectangle r = new Rectangle((int)Location.getX() - ClassDemoForPlatformerLab.CameraX , (int)Location.getY(), (int)Size.getX(), (int)Size.getY());
		//((Graphics2D) g).fill(r);
		g.setColor(Color.WHITE);
		g.drawRect((int)Location.getX() - GameEngine.Camera.camera.getX(), (int)Location.getY() - GameEngine.Camera.camera.getY(), (int)Size.getX(), (int)Size.getY());
	}
}

/**
 * Helper class for storing Sprite sheets and setting currently active frame in the animation.
 * @author Laughlin
 */
class Sprite implements Serializable
{
	public BufferedImage spriteSheet;
	public BufferedImage currentFrame;
	public int animationNumber = 1;
	public int frameNumber = 0;
	//!!!: These numbers are customized to the provided sprite sheet, if you use additional sprite sheets you may need to create a constructor for this and set the numbers accordingly 
	public int tileXSize = 120;
	public int tileYSize = 147;
	public static final int FRAME_NUMBER = 9;
	
	public Sprite(String fileName)
	{	
		try {
			spriteSheet = ImageIO.read(getClass().getResource(fileName +".png"));    //(new File(fileName +".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}	
		//init currentFrame
		currentFrame = spriteSheet.getSubimage(0,0, tileXSize, tileYSize);
	}
	public void Update()
	{
		frameNumber = (frameNumber + 1) % FRAME_NUMBER;
		currentFrame = spriteSheet.getSubimage(frameNumber*tileXSize, animationNumber*tileYSize, tileXSize, tileYSize);
	}	
}
class PlatPlayer implements KeyListener,  Serializable
{	
	public Color C;
	//These variables are how much we are going to change the current X and Y per loop
	public float speed;
	public Vector2D Velocity;
	public Vector2D Location;
	public Vector2D Size = new Vector2D(50,70);
	public final Vector2D GRAVITY = new Vector2D(0,1);
	
	public static final int MAX_X_VELOCITY = 28, JUMP_VELOCITY = 27;
	
	public boolean UP, DOWN, RIGHT, LEFT, GROUND, SPACE;
	
	public Sprite sprite = new Sprite("ninjaGirl");
	
	enum facing
	{
		FacingRight,
		FacingLeft,
	}
	facing direction;	
	
	public PlatPlayer(int x, int y, Color c)
	{
		speed = .4f;
		Velocity = new Vector2D();
		Location = new Vector2D(x,y);		
		C=c;
	}	
	
	//This makes it so updates are not spam'ed too frequently
	long lastUpdate = 0;
	public void MoveBall()
	{
		//Action
		if (SPACE)
		{				
			//TODO: Add action control
			SPACE = false;
		}
		
		//update the ball's current location			
			Location = Location.add(Velocity.multiply(speed));
		
		//if I'm jumping then I'm not on the ground
		if (Velocity.getY() < 0) //I'm moving up so I can't be on the ground
			GROUND = false;
		
		//respond to movement keys
			if (UP && GROUND) //UP also known as JUMP
			{
				Velocity = Velocity.add(new Vector2D(0,-JUMP_VELOCITY));
				GROUND = false;
			}
			if (LEFT)
			{
				if (GROUND)
					Velocity = Velocity.add(new Vector2D(-2,0));
				else
					Velocity = Velocity.add(new Vector2D(-0.8f,0));
			}
			if (RIGHT)
			{
				if (GROUND)
					Velocity = Velocity.add(new Vector2D(2,0));
				else
					Velocity = Velocity.add(new Vector2D(0.8f,0));
			}
		//handle animations
			boolean moving = true;
			
			//running left and right
				if (Velocity.getX() > 0.8)
				{
					direction = facing.FacingRight;
					sprite.animationNumber = 1;
				}
				else if (Velocity.getX()<-0.8)
				{
					direction = facing.FacingLeft;
					sprite.animationNumber = 4;
				}
				else
					moving = false;
			//jumping
			if (Math.abs(Velocity.getY()) > 0.2)
			{
				if ( direction == facing.FacingRight)
					sprite.animationNumber = 2;
				else if (direction == facing.FacingLeft)
					sprite.animationNumber = 5;
				moving = true;
			}
				
			//Animation direction
			if (System.currentTimeMillis() - lastUpdate > 100)
			{
				lastUpdate = System.currentTimeMillis();
			
				if (!moving && direction == facing.FacingRight)
					sprite.animationNumber = 0;
				else if (!moving && direction == facing.FacingLeft)
					sprite.animationNumber = 3;
				sprite.Update();
			}
			
			//If on the ground
			if (GROUND)
				Velocity = Velocity.multiply(.9f);//Friction
			else //!ground also know as air
			{
				//Velocity = Velocity.multiply(.99f);//Friction
				if (Math.abs(Velocity.getX()) > MAX_X_VELOCITY)
					Velocity.setX(Velocity.getX() > 0 ? MAX_X_VELOCITY : -MAX_X_VELOCITY);//Don't do this except in platformers
			}
			
		//Gravity thou art a crewl b....
			Velocity = Velocity.add(GRAVITY);
			GROUND = false;
			
		//Did I fall off
			if (Location.getY() > 5000) //Then you dead yo
				Die();				
	}
	/**
	 * remove lives and reset to a starting or saved position
	 */
	public void Die() 
	{ //resets location to a starting point
		Location = new Vector2D(100,100);
		Velocity = new Vector2D(0,0);
	}
	final float MTD_THRESHOLD = 5.0f;
	public void checkCollision(Collidable c)
	{
		Ellipse2D.Float myCollision = new Ellipse2D.Float(Location.getX()-Size.getX()/2,Location.getY()-Size.getY()/2, Size.getX(),Size.getY());
		
		//These collision points may need to be adjusted for other sprite (Sizes and Shapes)
		float collisionSize = Size.getX()/8;
		float startXCollision = Size.getX()/4; //10
		float endXCollision = Size.getX()*3/4; //30
		float XCollisionWidth = endXCollision - startXCollision;
		float OffsetStartCollisionX = Size.getX()*3/8; //15
		float OffsetStartCollisionY = Size.getY()*3/8; //15
		
		float startXLeftCollision =  Size.getX()/2;
		float startYCollision = Size.getY()/4;
		float endYCollision = Size.getY()*3/4; //30
		float YCollisionWidth = endYCollision - startYCollision;
		
		if (myCollision.intersects(c.getCollision()))
		{
			//I hit something but where?
			Ellipse2D.Float bottomCollision = new Ellipse2D.Float((int)Location.getX()-startXCollision,(int)Location.getY()+OffsetStartCollisionY, XCollisionWidth,collisionSize);
			if (Velocity.getY() > 0 && bottomCollision.intersects(c.getCollision()))
			{
				//then i'm standing on something				
					Velocity.setY(0);
					//handle minimum translation distance (may need to change if size of objects are controlled differently)
					if (this.Location.getY() > c.getCollision().y - Size.getY()/2 + MTD_THRESHOLD)
						this.Location.setY(c.getCollision().y - Size.getY()/2);
					GROUND = true;
			}
			Ellipse2D.Float rightCollision = new Ellipse2D.Float((int)Location.getX()+OffsetStartCollisionX,(int)Location.getY()-startYCollision, collisionSize,YCollisionWidth);
			if (Velocity.getX() > 0 && rightCollision.intersects(c.getCollision()))
			{
				//then i'm running into something on the right				
					Velocity.setX(0);
			}
			Ellipse2D.Float leftCollision = new Ellipse2D.Float((int)Location.getX()-startXLeftCollision,(int)Location.getY()-startYCollision, collisionSize,YCollisionWidth);
			if (Velocity.getX() < 0 && leftCollision.intersects(c.getCollision()))
			{
				//then i'm running into something on the left			
					Velocity.setX(0);
			}
		}
	}
	public void Draw(Graphics g)
	{
		g.setColor(C);
		g.setFont(new Font("Arial",Font.BOLD,20));
		g.drawString("Location: "+Location, 15,55);
		g.drawString("Velocity: "+Velocity, 15,80);
		//g.fillOval((int)(Location.getX() - Size.getX()/2 - Project2.Camera.camera.getX()),(int)(Location.getY() - Size.getY()/2 - Project2.Camera.camera.getY()), (int)Size.getX(), (int)Size.getY());	
			
		g.drawImage(sprite.currentFrame.getScaledInstance((int)Size.getX(), (int)Size.getY(), BufferedImage.SCALE_FAST),
					(int)(Location.getX() - Size.getX()/2 - GameEngine.Camera.camera.getX()),(int)(Location.getY() - Size.getY()/2 - GameEngine.Camera.camera.getY()),null);

	}
	//KeyListener
		public void keyTyped(KeyEvent e) { /*do nothing*/ }

	    /** Handle the key-pressed event from the text field. */
	    public void keyPressed(KeyEvent e) 
	    { 
	    	if (e.getKeyCode() == KeyEvent.VK_W)
	    		UP=true;
	    	if (e.getKeyCode() == KeyEvent.VK_S)
	    		DOWN=true;
	    	if (e.getKeyCode() == KeyEvent.VK_D)
	    		RIGHT=true;
	    	if (e.getKeyCode() == KeyEvent.VK_A)
	    		LEFT=true;
	    	if (e.getKeyCode() == KeyEvent.VK_SPACE)
	    		SPACE=true;
	    }
	    
	    /** Handle the key-released event from the text field. */
	    public void keyReleased(KeyEvent e) 
	    {
	    	if (e.getKeyCode() == KeyEvent.VK_W)
	    		UP=false;
	     	if (e.getKeyCode() == KeyEvent.VK_S)
	    		DOWN=false;
	    	if (e.getKeyCode() == KeyEvent.VK_D)
	    		RIGHT=false;
	    	if (e.getKeyCode() == KeyEvent.VK_A)
	    		LEFT=false;
	    	if (e.getKeyCode() == KeyEvent.VK_SPACE)
	    		SPACE=false;
	    }
}

//helper class for keeping track of "math" vector information in 2D
class Vector2D 
{
  private float x;
  private float y;

  public Vector2D() 
  {
      this.setX(0);
      this.setY(0);
  }
  
  public Vector2D(float x, float y) 
  {
      this.setX(x);
      this.setY(y);
  }
  public Vector2D(Vector2D v) 
  {
      this.setX(v.getX());
      this.setY(v.getY());
  }
  
  public static double Distance(Vector2D position2, Vector2D position3) 
	{
		return Math.sqrt(Math.pow(position2.getX()-position3.getX(),2) + Math.pow(position2.getY()-position3.getY(),2));
	}
  public double Distance(Vector2D position3) 
	{
		return Math.sqrt(Math.pow(getX()-position3.getX(),2) + Math.pow(getY()-position3.getY(),2));
	}
  
  public void set(float x, float y) 
  {
      this.setX(x);
      this.setY(y);
  }

  public void setX(float x) 
  {
      this.x = x;
  }

  public void setY(float y) 
  {
      this.y = y;
  }

  public float getX() 
  {
      return x;
  }

  public float getY() 
  {    	
      return y;
  }
  public void rotate(double angle) 
  {
  	Vector2D newVect = new Vector2D(this);
		newVect.setX(getX() * (float)Math.cos(Math.toRadians(angle)) + 
				getY() * (float)Math.sin(Math.toRadians(angle)));
		newVect.setY(-getX() * (float)Math.sin(Math.toRadians(angle)) + 
				getY() * (float)Math.cos(Math.toRadians(angle)));
		this.set(newVect.getX(),newVect.getY());
  }
  //U x V = Ux*Vy-Uy*Vx
  public static float Cross(Vector2D U, Vector2D V)
  {
  	return U.x * V.y - U.y * V.x;
  }
  public float dot(Vector2D v2) 
  {
  	float result = 0.0f;
      result = this.getX() * v2.getX() + this.getY() * v2.getY();
      return result;
  }

  public float getLength() 
  {
      return (float) Math.sqrt(getX() * getX() + getY() * getY());
  }

  public Vector2D add(Vector2D v2) 
  {
      Vector2D result = new Vector2D();
      result.setX(getX() + v2.getX());
      result.setY(getY() + v2.getY());
      return result;
  }

  public Vector2D subtract(Vector2D v2) 
  {
      Vector2D result = new Vector2D();
      result.setX(this.getX() - v2.getX());
      result.setY(this.getY() - v2.getY());
      return result;
  }

  public Vector2D multiply(float scaleFactor) 
  {
      Vector2D result = new Vector2D();
      result.setX(this.getX() * scaleFactor);
      result.setY(this.getY() * scaleFactor);
      return result;
  }

  //Specialty method used during calculations of ball to ball collisions.
  public Vector2D normalize() 
  {
  	float length = getLength();
      if (length != 0.0f) 
      {
          this.setX(this.getX() / length);
          this.setY(this.getY() / length);
      } 
      else 
      {
          this.setX(0.0f);
          this.setY(0.0f);
      }
      return this;
  }
  public String toString()
  {
  	return "("+x+", "+y+")";
  }
}
