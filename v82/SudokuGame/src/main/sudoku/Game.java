package main.sudoku;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Objects;

import javax.swing.JFrame;

//import com.game.src.main.KeyInput;

//import com.game.src.main.MouseInput;

public class Game extends Canvas implements Runnable{

	
	
	//game data==static variables
		//-------puzzle controller, used to control everything relate to the puzzle
		static PuzzleController PC;//=new PuzzleController();//include map backup map and the control variables
		//this is puzzle generation control class for object to create a new thread for every new game of puzzle generation
		//every time player requests a brand new game series(by selecting easy normal hard), the game will create a PuzzleController and
		//PC will as a reference will point to that PuzzleController. The PuzzleController will be there as long as the player stay in same
		//game mode (or said same game series/ which mean never click return to menu button). Every time if the player request a new puzzle
		// the PuzzleController will create a new thread for generating a new puzzle.
		//when player returns to menu. The PC pointer will abandon the PuzzleController by setting the pointing location to null. This is
		//because if the player return to menu, the next start will be brand new game round, a new PuzzleController will be created to control
		//a new series of generation. In this progress, a new map1 and map2 will be created, everything will be set to initial states. If in older
		//PuzzleController, there is a thread does not finish its job, it will be put aside, and after it is done, the thread will be terminated 
		//automatically, and the older PuzzleController will wait to be recycled.
		//As long as every time the PuzzleController and the thread are new, the older objects will no affect the game at all.
		
		//--------mouse selection locations
		static int mousex;
		static int mousey;
		static boolean isMouseDown;
		//--------for mouse hover
		static int mousehx;
		static int mousehy;
		//--------key pressed record
		static int keynumber=-1;
		//--------game status--0: main screen menu; 1: default mode; 2:challenge time mode; 1.5 & 2.5 means preparation for each mode
		static double gameStat=0;
		static boolean showSecondaryMenuForSt=false;//if show secondary menu in main menu for 
		static boolean showSecondaryMenuForCh=false;
		
	//===========
		Button BStandardMode =new Button(220,110,200,80,false);
		Button BChallengeMode =new Button(220,210,200,80,false);
		Button BQuit =new Button(220,310,200,80,false);
		
		Button BNewSt = new Button(210,110,100,80,true);
		Button BNewCh = new Button(210,210,100,80,true);
		Button BContSt = new Button(330,110,100,80,true);
		Button BContCh = new Button(330,210,100,80,true);
		
		Button BEasy =new Button(220,110,200,80,false);
		Button BNormal =new Button(220,210,200,80,false);
		Button BHard =new Button(220,310,200,80,false);
		
		Button BDelete=new Button(440,210,85,50,false);
		Button BClear=new Button(535,210,85,50,false);
		Button BClearConfirm = new Button(535,210,85,50,false);boolean CConfirming=false;
		Button BGiveup=new Button(440,270,180,50,false);
		Button BGiveupConfirm=new Button(440,270,180,50,false);boolean GUConfirming=false;
		Button BSwitchRP; boolean showSolv=false;//this variable used to switch the puzzle and solution, default is true to show solution right after failed
		Button BNextPuzzle = new Button(440,270,180,50,false);
		Button BReturntoMenu = new Button(440,330,180,50,false);
		
		Button[] BNumPad=new Button [9];
	////////////////////////////////////////////////////////////////////
	//=====graphic fade in====use alpha to control the fade in
		int alphaFadeEffect=255;
		int alphaFadeEffectForMap=0;
		int interfaceFadeRate=10;
		int mapFadeRate=3;
	//----------------------------
		
	Thread thread; //the secondary thread
	boolean running;
	
	private BufferedImage image  = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_RGB);
	
	
	
	
	private synchronized void start()
	{
		
		if(running)
			return;
		running =true;	
		thread =new Thread(this);
		thread.start();
	}
	private synchronized void stop() 
	{
		if(!running)
			return;
		
		running = false;
		try 
		{
			thread.join();
		} catch (InterruptedException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(1);
	}
	public void run()
	{
		//load everything graphic or need in this thread to prepare here
		////////////////////////////////////////
		
		this.addMouseListener(new MouseInput());
		this.addKeyListener(new KeyInput());
		this.addMouseMotionListener(new MouseMotion());
		
		
		///////////////////////
		for(int i=0;i<9;i++)//initiate number pad buttons
		{
			this.BNumPad[i]=new Button(391,21+i*40,38,38,false);
		}
		
		//those are for frame calculation & limitations
		long lastTime = System.nanoTime();
		final double amountOfTicks = 60.0; //the game will update 60 times in one second (limit with 60 fps)
		double ns = 1000000000 / amountOfTicks;
		double delta = 0;
		int updates = 0;
		int frames = 0;//this frame values is if the game has unlimited fps
		long timer = System.currentTimeMillis(); // save the start time
		while(running)
		{
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			if(delta >= 1)
			{
				tick(); //update the calculations and every values per frame 
				updates++;
				
				delta-=(int)delta;
			}
			render();//render graphic every computation
			frames++;
			
			if(System.currentTimeMillis()-timer>=1000) //when time pass 1 second show the "updates" tricks and "frames" fps. fps(loop time in 1 second)
			{
				//timer += 1000; //update timer to current time when it pass 1 second;
				timer = System.currentTimeMillis();
				System.out.println("game state: "+ gameStat+" "+ Thread.activeCount() +" " + Thread.currentThread());
				System.out.println(updates +" Ticks, Fps " + frames);
				updates = 0;
				frames = 0;
			}
		}
		
		this.stop();//when the program ended, end the thread
	}
	
	private void tick()//calculations for user input and game responds (update / frame)
	{
		//System.out.println(difficulty);
		
		tickGameMode0();
		tickGameMode1_2PreStart();
		tickGameMode1_2Start();
		
	}
	
	private void render()//rendering
	{
		BufferStrategy bs = this.getBufferStrategy();
		
		if(bs == null)
		{
			createBufferStrategy(3); //create three "buffers"(screen and two loading screen)
			return;
		}
		Graphics g =bs.getDrawGraphics();
		
		g.drawImage(image, 0, 0, getWidth(), getHeight(), this);  //draw image
		
		renderGameMode0(g);
		
		renderGameMode1_2PreStart(g);
		
		renderGameMode1_2Start(g);
		
		
		//let's do a fade-in affect (use a black rect to block the graphic on the top and faded away)
		if(alphaFadeEffect>=interfaceFadeRate)
		{
			g.setColor(new Color(0,0,0,alphaFadeEffect-=interfaceFadeRate));
			g.fillRect(0, 0, 640, 320/12*9*2);//this.WIDTH;
		}
		else 
			alphaFadeEffect=0;
		
		if(alphaFadeEffectForMap >=mapFadeRate && PC!=null)
		{
			g.setColor(new Color(0,0,0,alphaFadeEffectForMap-=mapFadeRate));
			g.fillRect(PC.puzzleStartPosX, PC.puzzleStartPosY, PC.puzzleWidth, PC.puzzleHeight);
			g.setColor(new Color(255,255,255));
			g.drawRect(PC.puzzleStartPosX, PC.puzzleStartPosY, PC.puzzleWidth, PC.puzzleHeight);
		}
		else
			alphaFadeEffectForMap=0;
		///////////////////////////////////
		g.dispose(); //dispose g
		bs.show(); //show the buffers
				
	}
	
	public static void main(String[] args) {//here is 1st thread calculation for map generation
		// TODO Auto-generated method stub
		Game game =new Game();
		
		//ArrayList<PuzzleGenerationControl> PGCL=new ArrayList<PuzzleGenerationControl>();
		
		game.setPreferredSize(new Dimension(640, 320/12*9*2));//640
		game.setMaximumSize(new Dimension(640, 320/12*9*2));
		game.setMinimumSize(new Dimension(640, 320/12*9*2));
		
		JFrame frame = new JFrame("Sudoku");
		frame.add(game); //connect game and frame
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //close the process when the frame is closed
		frame.setResizable(false); //can't change the size of frame
		frame.setLocationRelativeTo(null);
		frame.setVisible(true); //show the frame
		frame.addWindowListener(new WindowAction());
		//////////////////////////////initiate every values

		mousex=0;
		mousey=0;
		isMouseDown=false;
		
		mousehx=0;
		mousehy=0;
		///////////////////////////////
		
		game.start();//here 2nd thread start
		
		///////////////////////////////
		
		
		///updating extra info if needed
		/*
		double timen=System.nanoTime();
		while(game.running)
		{
			
			
			if(System.nanoTime()-timen>=1000000000) 
			{
				System.out.println("Game state: "+ gameStat +" "+Thread.activeCount()+" "+Thread.currentThread());
				timen=System.nanoTime();
				
				//System.out.println("que size: "+PC.size());
			}
			if(gameStat==0)//those values always be false under game state =0
			{
				
			}
			
			if((gameStat==1 || gameStat==2) && difficulty!=-1 )//&& isPC==false)//&& PGCLSize==PGCL.size())
			{
				
				
			}
			
			
		}
		*/
	}

	private void tickGameMode0()
	{
		if(gameStat==0)///when at main menu-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=
		{
			
			if(this.BStandardMode.isMouseClickedIn(mousex, mousey,isMouseDown) && !showSecondaryMenuForSt)// && isMouseDown)//default mode--when mouse is clicking this area
			{
				showSecondaryMenuForSt=true;

				this.BNewSt.resetAlphas();//and reset fade in alpha
				this.BContSt.resetAlphas();
			}
			else if(showSecondaryMenuForSt)//secondary menu for standard mode
			{
				if(this.BNewSt.isMouseClickedIn(mousex, mousey, isMouseDown))
				{
					gameStat=1.5;
					showSecondaryMenuForSt=false;
					
					
					alphaFadeEffect=255;
					
				}
				else if(SaveLoad.fileExistSt() && this.BContSt.isMouseClickedIn(mousex, mousey, isMouseDown))
				{//if pressed continue button, load the game,recover needed, and start the game
					gameStat=1;//set the game state to 1 to start
					
					PC=new PuzzleController(-1,20,20,380,380);//create new instance of PC for the game(as difficulty,set it to -1 because havent know that yet, however, sl will set it automatically)
					SaveLoad sl=new SaveLoad();//load from save file
					sl.loadSt(PC);
					PC.SetCompleteForLoading();//autoset iscurrentsolv to true if the puzzle is already complete without increment records (because record should already increment last time before player exit the game)
					showSecondaryMenuForSt=false;//disable the secondary menu (next time reenter state 0 will not show up secondary menu)
					
					BSwitchRP=new Button(PC.puzzleStartPosX,PC.puzzleStartPosY,PC.puzzleWidth,PC.puzzleHeight,true);
					
					showSolv=false;
					
					alphaFadeEffect=255;
				}
			}
			if(!this.BNewSt.isMouseInside(mousex, mousey)&&!this.BContSt.isMouseInside(mousex, mousey)&&!this.BStandardMode.isMouseInside(mousex, mousey) && isMouseDown)
			{
				showSecondaryMenuForSt=false;//if mouse click somewhere else hide the secondary menu
				
			}
			
			//----------------------------------------------------------------------------
			
			if(this.BChallengeMode.isMouseClickedIn(mousex, mousey,isMouseDown) && !showSecondaryMenuForCh)// && isMouseDown)//challenge mode--when mouse is clicking this area
			{
				showSecondaryMenuForCh=true;

				this.BNewCh.resetAlphas();
				this.BContCh.resetAlphas();
			}
			else if(showSecondaryMenuForCh)//secondary menu for challenge mode
			{
				if(this.BNewCh.isMouseClickedIn(mousex, mousey, isMouseDown))
				{
					gameStat=2.5;
					showSecondaryMenuForCh=false;
					
					
					alphaFadeEffect=255;
				}
				else if(SaveLoad.fileExistCh() && this.BContCh.isMouseClickedIn(mousex, mousey,isMouseDown))
				{//if pressed continue button, load the game,recover needed, and start the game
					gameStat=2;
					
					PC=new PuzzleController(-1,20,20,380,380);
					SaveLoad sl=new SaveLoad();
					sl.loadCh(PC);
					PC.SetCompleteForLoading();
					showSecondaryMenuForCh=false;
					
					BSwitchRP=new Button(PC.puzzleStartPosX,PC.puzzleStartPosY,PC.puzzleWidth,PC.puzzleHeight,true);
					
					showSolv=false;
					
					alphaFadeEffect=255;
				}	
			}
			if(!this.BNewCh.isMouseInside(mousex, mousey)&&! this.BContCh.isMouseInside(mousex, mousey)&&!this.BChallengeMode.isMouseInside(mousex, mousey)&& isMouseDown)
			{
				showSecondaryMenuForCh=false;//if mouse click somewhere else hide the secondary menu
				
			}
			
			
			//------------------------------------------------------
			if(this.BQuit.isMouseClickedIn(mousex, mousey, isMouseDown))//quit--when mouse is clicking this area
			{
				System.exit(1);
				
			}
			
			
			
		}
	}
	
	private void renderGameMode0(Graphics g) {
		if(gameStat==0)///when at main menu=-=-=-=-==--=-==-=-=-=-=-=-=-==-==-=-
		{
			//title
			g.setFont(new Font("TimesRoman", Font.PLAIN, 75));
			g.setColor(new Color(255,255,255));
			int fWidth=g.getFontMetrics().stringWidth("Sudoku Game");
			g.drawString("Sudoku Game", (640-fWidth)/2, 80);
			
			//new game button
			if(!showSecondaryMenuForSt)
			{
				this.BStandardMode.renderHoverButton(mousehx, mousehy, new Color(0,0,255), g);//hover
				
				this.BStandardMode.renderButton(new Color(255,255,255),g);
				
				this.BStandardMode.setText("Standard", new Font("TimesRoman", Font.PLAIN, 40), true, new Color(255,255,255), g);
				
			}
			else if(showSecondaryMenuForSt)//secondary menu for standard mode
			{
				this.BNewSt.renderHoverButton(mousehx, mousehy, new Color(255,0,255), g);
				this.BNewSt.renderButton(new Color(255,255,255), g);
				
				this.BNewSt.setText("New", new Font("TimesRoman", Font.PLAIN, 20),true, new Color(255,255,255), g);
				
				if(SaveLoad.fileExistSt())
				{
					this.BContSt.renderHoverButton(mousehx, mousehy, new Color(255,0,255), g);
					this.BContSt.renderButton(new Color(255,255,255), g);
					
					this.BContSt.setText("Continue", new Font("TimesRoman", Font.PLAIN, 20), true, new Color(255,255,255), g);
				}
				else
				{
					this.BContSt.renderButton(new Color(100,100,100), g);
					
					this.BContSt.setText("Continue", new Font("TimesRoman", Font.PLAIN, 20), true, new Color(100,100,100), g);
					
				}
			}
			
			
			//challenge game mode button
			if(!showSecondaryMenuForCh)
			{
				this.BChallengeMode.renderHoverButton(mousehx, mousehy, new Color(0,0,255), g);//hover
				
				this.BChallengeMode.renderButton(new Color(255,255,255),g);
				
				this.BChallengeMode.setText("Challenge", new Font("TimesRoman", Font.PLAIN, 40), true, new Color(255,255,255), g);
				
			}
			else if(showSecondaryMenuForCh)//secondary menu for challenge mode
			{
				this.BNewCh.renderHoverButton(mousehx, mousehy, new Color(255,0,255), g);
				this.BNewCh.renderButton(new Color(255,255,255), g);
				
				this.BNewCh.setText("New", new Font("TimesRoman", Font.PLAIN, 20), true, new Color(255,255,255), g);
				
				if(SaveLoad.fileExistCh())
				{
					this.BContCh.renderHoverButton(mousehx, mousehy, new Color(255,0,255), g);
					this.BContCh.renderButton(new Color(255,255,255), g);
					
					this.BContCh.setText("Continue", new Font("TimesRoman", Font.PLAIN, 20), true, new Color(255,255,255), g);
				}
				else
				{
					this.BContCh.renderButton(new Color(100,100,100), g);
					
					this.BContCh.setText("Continue", new Font("TimesRoman", Font.PLAIN, 20), true, new Color(100,100,100), g);
				}
			}
			
			//quit button
			this.BQuit.renderHoverButton(mousehx, mousehy, new Color(0,0,255), g);
			
			this.BQuit.renderButton(new Color(255,255,255), g);
			
			this.BQuit.setText("Quit", new Font("TimesRoman", Font.PLAIN, 40), true, new Color(255,255,255), g);
			
			//some random text
			g.setFont(new Font("TimesRoman", Font.PLAIN, 20));
			g.setColor(new Color(100,100,100));
			fWidth=g.getFontMetrics().stringWidth("By Pseudocode Company V 0.081");
			g.drawString("By Pseudocode Company V 0.082", (640-fWidth)/2, 410+50);
			
			
			
			
		}
	}
	
	private void tickGameMode1_2PreStart() //(preset)this is for preparation before game start set every thing into pre-start state
	{
		if((gameStat==1.5 || gameStat==2.5))///when selected game mode however havent selected difficulty
		{
			if(this.BEasy.isMouseClickedIn(mousex, mousey, isMouseDown))//easy--when mouse is clicking this area
			{
				gameStat=gameStat-0.5;
				
				
				
				//when every time a player click on difficulty, which means that the game should start a new puzzle generation. So a new puzzle controller is created for this.
				PC=new PuzzleController(0,20,20,380,380);//create a PC with easy difficulty
				
				PC.initGenerationControl();//new generation thread start here(3rd thread)
				
				BSwitchRP=new Button(PC.puzzleStartPosX,PC.puzzleStartPosY,PC.puzzleWidth,PC.puzzleHeight,true);
				
				showSolv=false;
				
				alphaFadeEffect=255;
			}
			if(this.BNormal.isMouseClickedIn(mousex, mousey,isMouseDown))//normal--when mouse is clicking this area
			{
				gameStat=gameStat-0.5;
			
				
				//when every time a player click on difficulty, which means that the game should start a new puzzle generation. So a new puzzle controller is created for this.
				PC=new PuzzleController(1,20,20,380,380);//create a PC with normal difficulty
				
				PC.initGenerationControl();//new generation thread start here(3rd thread)
				
				BSwitchRP=new Button(PC.puzzleStartPosX,PC.puzzleStartPosY,PC.puzzleWidth,PC.puzzleHeight,true);
				
				showSolv=false;
				
				alphaFadeEffect=255;
				
			}
			if(this.BHard.isMouseClickedIn(mousex, mousey,isMouseDown))//hard--when mouse is clicking this area
			{
				gameStat=gameStat-0.5;
				
				
				//when every time a player click on difficulty, which means that the game should start a new puzzle generation. So a new puzzle controller is created for this.
				PC=new PuzzleController(2,20,20,380,380);//create a PC with hard difficulty
				
				PC.initGenerationControl();//new generation thread start here(3rd thread)
				
				BSwitchRP=new Button(PC.puzzleStartPosX,PC.puzzleStartPosY,PC.puzzleWidth,PC.puzzleHeight,true);
				
				showSolv=false;
				
				alphaFadeEffect=255;
			}
			
		}
	}
	
	private void renderGameMode1_2PreStart(Graphics g) {
		if(gameStat==1.5 || gameStat==2.5)///when select game mode however havent select difficulty
		{
			//easy
			this.BEasy.renderHoverButton(mousehx, mousehy, new Color(0,200,0), g);
			
			this.BEasy.renderButton(new Color(255,255,255), g);
			
			this.BEasy.setText("Easy", new Font("TimesRoman", Font.PLAIN, 40), true, new Color(255,255,255), g);
			
			//normal
			this.BNormal.renderHoverButton(mousehx, mousehy, new Color(200,200,0), g);
			
			this.BNormal.renderButton(new Color(255,255,255), g);
			
			this.BNormal.setText("Normal", new Font("TimesRoman", Font.PLAIN, 40), true, new Color(255,255,255), g);
			
			//hard
			this.BHard.renderHoverButton(mousehx, mousehy, new Color(255,0,0), g);
			
			this.BHard.renderButton(new Color(255,255,255), g);
			
			this.BHard.setText("Hard", new Font("TimesRoman", Font.PLAIN, 40), true, new Color(255,255,255), g);
		}
	}
	
	private void tickGameMode1_2Start()
	{
		if(gameStat==1 || gameStat==2)///when the game start-=-=-=-=-=-==--=-=-=-=-=-=-=-=-=-=
		{
			if(gameStat==2)//run following only if gamestat==2
			{//by putting this method here, the game could update isFail in PC until fail state(isFail) is set to be true
				if(PC.isGen && !PC.isCurrentSolv && !PC.isFail)//after loading complete and the game not end yet, do the failure calculations
					PC.autoSetfailedTime();
			}
			//input functions-------------------------------------------
			if(PC.isFail)//if fail state has been set, block every input to the puzzle 
			{
				//after fail player should able to switch between puzzle and solutions
				if(BSwitchRP.isMouseClickedIn(mousex, mousey, isMouseDown))
				{//the puzzle board should be effectively a button when failed
					alphaFadeEffectForMap=255;
					if(showSolv)
					{
						showSolv=false;
					}
					else
						showSolv=true;
				}
			}
			else if(PC.isCurrentSolv)//when player solved the puzzle and did not click next or return to menu, the puzzle map will be unable to change
			{
				
			}
			else if(PC.isGen)//after puzzle is generated, player can input numbers. And after fail or completely solved, player can no longer progress the game
			{
				PC.puzzleInput(mousex, mousey, keynumber,isMouseDown);//remember, puzzleInput will not allow player to change the numbers of original puzzle itself
				keynumber=-1;
			}
			
			
			
			
			//=======reset PC selector=========
			if(isMouseDown)//to reset PC selectors if mouse click on some not related position
			{
				boolean isOnNumPad=false;

				if(this.BNumPad[0].posx<=mousex&&mousex<this.BNumPad[this.BNumPad.length-1].posEndx && this.BNumPad[0].posy<=mousey&&mousey<this.BNumPad[this.BNumPad.length-1].posEndy)
				{
					isOnNumPad=true;
				}
				if(!(PC.puzzleStartPosX<=mousex &&mousex<PC.puzzleEndPosX  &&  PC.puzzleStartPosY<=mousey &&mousey<PC.puzzleEndPosY ) 
						&&!isOnNumPad && !this.BDelete.isMouseInside(mousex, mousey))
				{
					PC.selectCellX=-1;
					PC.selectCellY=-1;
				}
			}
			
			//================
			
			//regular buttons==========================================================================================
			
			//number pad-----------------------------------
			
			for(int i=0;i<9;i++)
			{
				if(this.BNumPad[i].isMousePressingIn(mousex, mousey, isMouseDown))
				{
					keynumber=i+48+1;//number pad will also control the input keynumber variable
					
				}
			}
			//delete button
			if(!(PC.isCurrentSolv||PC.isFail) && PC.isGen && this.BDelete.isMouseClickedIn(mousex, mousey,isMouseDown))
			{
				
				PC.deleteSelectCell();//call the method to clear player fill in the selected cell
				
			}
			
			//clear the filled cell button
			if(!(PC.isCurrentSolv||PC.isFail)&& !CConfirming && PC.isGen && this.BClear.isMouseClickedIn(mousex, mousey,isMouseDown))
			{
			
				CConfirming=true;
				
			}
			else if(!(PC.isCurrentSolv||PC.isFail)&&CConfirming && PC.isGen && this.BClearConfirm.isMouseClickedIn(mousex, mousey, isMouseDown))
			{
				PC.clearMap1();//call the method to clear player fill in cells
				alphaFadeEffectForMap=255;
				CConfirming=false;//reset clean confirmation after cleaning
			}
			else if((!this.BClearConfirm.isMouseInside(mousex, mousey) && isMouseDown) ||(PC.isCurrentSolv||PC.isFail))
			{
				CConfirming=false;//reset clean confirmation when mouse pressed somewhere else, or game end 
			}
			
			//next puzzle & giveup function--------------------------------------when mouse is clicking this area
			if((PC.isCurrentSolv ||PC.isFail) && PC.isBackupGen && this.BNextPuzzle.isMouseClickedIn(mousex, mousey,isMouseDown) )
			{
				PC.contGenerationControl();//start new map generation thread for continue condition
				showSolv=false;
				alphaFadeEffectForMap=255;
			}
			else if(!(PC.isCurrentSolv ||PC.isFail) && PC.isBackupGen&&!GUConfirming&& this.BGiveup.isMouseClickedIn(mousex, mousey,isMouseDown))
			{
				GUConfirming=true;
			}
			else if(!(PC.isCurrentSolv ||PC.isFail) && PC.isBackupGen&& GUConfirming && this.BGiveupConfirm.isMouseClickedIn(mousex, mousey, isMouseDown))
			{
				PC.setFail();
				showSolv=true;
				alphaFadeEffectForMap=255;
				GUConfirming=false;
			}
			else if((!this.BGiveupConfirm.isMouseInside(mousex, mousey)&&isMouseDown) ||(PC.isCurrentSolv ||PC.isFail))
			{
				GUConfirming=false;
			}
			
			//====regular buttons end===================================================================
			
			//===set complete==== put it here after everything is checked except return to menu
			if(PC.isGen && !PC.isFail && !PC.isCurrentSolv)//when the PC is gen and game not end yet
				PC.autoSetComplete(); //when player solved the puzzle, auto set iscurrentsolved to true
			//this method has the function to check conflict at same time, if this method is called here it will check completion and set conflict indicator every frame
			
			
			//ending check, check it exit the game========================================
			//return to menu function----------------------------when mouse is clicking this area---we put it here because if this is clicked, PC is deleted
			if(this.BReturntoMenu.isMouseClickedIn(mousex, mousey,isMouseDown))//&& isBackupGen 
			{
				//remember every return to menu needs to process those actions
				//here needs a saving progress
				if((Game.gameStat==1||Game.gameStat==2) && Game.PC.isGen && Game.PC.isBackupGen)
				{
					
					
					SaveLoad sl=new SaveLoad();
					sl.save(PC,gameStat);
				}
				/////////////======================
				
				gameStat=0;
				
				//in order to end thread quickly these process should be stopped immediately and prevent starting new one
				PC.MSG.stop=true;//stop every map generation
				PC.PG.stop=true;//stop every puzzle generation
				PC.PG.LS.stop=true;//stop every verify process in puzzle generation
				
				PC=null;//point the PC to null for reset
				
				alphaFadeEffect=255;
			}
			
			//======ending check end======================================================
			
			

			
		}
	}

	private void renderGameMode1_2Start(Graphics g) {
		
		if(gameStat==1|| gameStat==2)	///when the game start-=-=-=-=-=-==--=-=-=-=-=-=-=-=-=-=
		{
			//difficulty display
			PC.renderDifficulty(gameStat, 20, 380+40, g);
			
			//some random text
			g.setColor(new Color(100,100,100));
			g.drawLine(270,390, 270, 460);
			if(!PC.isFail && !PC.isCurrentSolv)//when game still playing
			{	
				g.setColor(new Color(200,200,200));
				g.setFont(new Font("TimesRoman", Font.PLAIN, 20));
				int fWidth = g.getFontMetrics().stringWidth("The rules of the game");
				g.drawString("The rules of the game", 620-fWidth, 380+30);
				
				g.setColor(new Color(100,100,100));
				g.setFont(new Font("TimesRoman", Font.PLAIN, 15));
				fWidth = g.getFontMetrics().stringWidth("Each number within 1-9 can only appear once");
				g.drawString("Each number within 1-9 can only appear once", 620-fWidth, 380+50);
				fWidth = g.getFontMetrics().stringWidth("in a row, column or box. Good Luck!");
				g.drawString("in a row, column or box. Good Luck!", 620-fWidth, 380+70);
			}
			else if(PC.isFail)//when game failed
			{
				if(showSolv)
				{
					g.setColor(new Color(200,200,200));
					g.setFont(new Font("TimesRoman", Font.PLAIN, 20));
					int fWidth = g.getFontMetrics().stringWidth("Puzzle Board Showing: Solution");
					g.drawString("Puzzle Board Showing: Solution", 620-fWidth, 380+30);
				}
				else
				{
					g.setColor(new Color(200,200,200));
					g.setFont(new Font("TimesRoman", Font.PLAIN, 20));
					int fWidth = g.getFontMetrics().stringWidth("Puzzle Board Showing: Your Answer");
					g.drawString("Puzzle Board Showing: Your Answer", 620-fWidth, 380+30);
				}
				
				g.setColor(new Color(100,100,100));
				g.setFont(new Font("TimesRoman", Font.PLAIN, 15));
				int fWidth = g.getFontMetrics().stringWidth("You can click on the puzzle board to switch");
				g.drawString("You can click on the puzzle board to switch", 620-fWidth, 380+50);
				fWidth = g.getFontMetrics().stringWidth("between your answer and the solution");
				g.drawString("between your answer and the solution", 620-fWidth, 380+70);

			}
			else//when game complete
			{
				g.setColor(new Color(200,200,200));
				g.setFont(new Font("TimesRoman", Font.PLAIN, 20));
				int fWidth = g.getFontMetrics().stringWidth("Congratulations!");
				g.drawString("Congratulations!", 620-fWidth, 380+30);
				
				g.setColor(new Color(100,100,100));
				g.setFont(new Font("TimesRoman", Font.PLAIN, 15));
				fWidth = g.getFontMetrics().stringWidth("You can press Next to next puzzle.");
				g.drawString("You can press Next to next puzzle.", 620-fWidth, 380+50);
			}
			
			//timer, or clock
			if(!PC.isFail && !PC.isCurrentSolv && PC.isGen)
				PC.renderTimer(gameStat, 440, 45, g);
			
			//records
			PC.renderRecords(gameStat,440,60,180,140, g);
			
			//puzzle stat panal (loading, complete, fail)
			PC.renderPuzzleStatus(440,45, g);
			
			//render puzzle or solution in each condition
			if(!PC.isFail && !PC.isCurrentSolv)//if the puzzle is not failed yet, alway show puzzle even showSolv is set to be true
			{
				if(PC.isGen)//when map1 gen over, allow mouse actions to display
					PC.renderPuzzleInputLabel(mousex, mousey, mousehx, mousehy, isMouseDown, g);
				PC.renderPuzzle(g);
			}
			else if(PC.isCurrentSolv)
			{
				PC.renderPuzzle(g);
			}
			else if(PC.isFail && showSolv)
				PC.renderSolution(g);
			else if(PC.isFail && !showSolv)
			{
				PC.renderPuzzle(g);//the render puzzle will automatically judge for fail and complete game to render the puzzle
				this.BSwitchRP.buttonFill(this.BSwitchRP.width, this.BSwitchRP.height, new Color(255,255,255,50), g);
			}
			
			
			///////////////////////////////////buttons/////////////////////////
			//delete Button
			if(PC.isGen && !(PC.isCurrentSolv||PC.isFail))
			{
				this.BDelete.renderHoverButton(mousehx, mousehy, new Color(100,100,100), g);
				this.BDelete.renderButton(new Color(255,150,0), g);
			
				this.BDelete.setText("Del",new Font("TimesRoman", Font.PLAIN, 30) , true, new Color(255,150,0), g);
			}
			else 
			{
				this.BDelete.renderButton(new Color(100,50,0), g);
			
				this.BDelete.setText("Del",new Font("TimesRoman", Font.PLAIN, 30) , true, new Color(100,50,0), g);
			}
			
			//clear button
			if(PC.isGen && !(PC.isCurrentSolv||PC.isFail) && !CConfirming)
			{
				this.BClear.renderHoverButton(mousehx, mousehy, new Color(100,100,100), g);
				this.BClear.renderButton(new Color(100,255,255), g);
				
				this.BClear.setText("Clear", new Font("TimesRoman", Font.PLAIN, 30), true, new Color(100,255,255), g);
			}
			else if(PC.isGen && !(PC.isCurrentSolv||PC.isFail) && CConfirming)
			{
				this.BClearConfirm.renderHoverButton(mousehx, mousehy, new Color(100,100,100), g);
				this.BClearConfirm.renderButton(new Color(100,255,255), g);
				this.BClear.setText("Sure?", new Font("TimesRoman", Font.PLAIN, 30), true, new Color(255,120,80), g);
			}
			else
			{
				this.BClear.renderButton(new Color(50,100,100), g);
				
				this.BClear.setText("Clear", new Font("TimesRoman", Font.PLAIN, 30), true, new Color(50,100,100), g);
			}
			
			//--this is Next Button & give up button render place
			//when this puzzle is solved or failed show Next button
			if(!PC.isBackupGen && !PC.isGen)//both map are not gen
			{
				this.BNextPuzzle.renderButton(new Color(100,100,100), g);
				
				this.BNextPuzzle.setText("Preloading...", new Font("TimesRoman", Font.PLAIN, 16), false, new Color(100,100,100), g);
			}
			else if(!PC.isBackupGen && PC.isGen)//if not complete or fail yet, and main map gen complete but backup map is not gen
			{//if the map1 is gen completely but backup havent gen completely yet
				this.BNextPuzzle.buttonFill((int)((PC.PG.progress)*180), this.BNextPuzzle.height, new Color(50,50,0), g);//progress bar
				this.BNextPuzzle.renderButton(new Color(100,100,100), g);
				
				this.BNextPuzzle.setText("Preloading...", new Font("TimesRoman", Font.PLAIN, 16), false, new Color(100,100,100), g);
			}
			else if((PC.isCurrentSolv ||PC.isFail)&& PC.isBackupGen)//both complete and fail while the backup map is generated completely will let the Next button show up
			{
				this.BNextPuzzle.renderHoverButton(mousehx, mousehy, new Color(100,100,100), g);
				
				this.BNextPuzzle.renderButton(new Color(255,255,255), g);
				
				this.BNextPuzzle.setText("Next", new Font("TimesRoman", Font.PLAIN, 30), true, new Color(255,255,255), g);
			
			}
			else if(!(PC.isCurrentSolv ||PC.isFail)&& PC.isBackupGen &&!GUConfirming){//both map generated and the game not end yet, show a give up button

				this.BGiveup.renderHoverButton(mousehx, mousehy, new Color(100,100,100), g);
				this.BGiveup.renderButton(new Color(255,255,255), g);
				this.BGiveup.setText("Give Up", new Font("TimesRoman", Font.PLAIN, 30), true, new Color(255,255,255), g);
				
			}
			else if(!(PC.isCurrentSolv ||PC.isFail)&& PC.isBackupGen &&GUConfirming)//give up confirming
			{
				this.BGiveupConfirm.renderHoverButton(mousehx, mousehy,new Color(100,100,100), g);
				this.BGiveupConfirm.renderButton(new Color(255,255,255), g);
				this.BGiveup.setText("Give Up and Show Solution?", new Font("TimesRoman", Font.PLAIN, 13), true, new Color(255,120,80), g);
			}
			//---Next Button & give up button end
			
			//return to menu button
			this.BReturntoMenu.renderHoverButton(mousehx, mousehy, new Color(100,100,100), g);
			
			this.BReturntoMenu.renderButton(new Color(255,255,255), g);
			
			this.BReturntoMenu.setText("Menu", new Font("TimesRoman", Font.PLAIN, 30), true, new Color(255,255,255), g);	
			
			//--number pad---
			
			for(int i=0;i<9;i++)
			{
				
				this.BNumPad[i].renderHoverButton(mousehx, mousehy, new Color(200,210,255), g);
				this.BNumPad[i].renderButton(new Color(150,150,150), g);
				
				this.BNumPad[i].setText(Integer.toString(i+1), new Font("TimesRoman", Font.PLAIN, 30), true, new Color(150,150,150), g);
			}
			g.drawLine(391, 20, 429, 20);
			g.drawLine(391, 380, 429, 380);
			//////////////////////////buttons end////////////////////////////////
			
			
			///////////////////////////////
		}
	}
	
	
	
}
