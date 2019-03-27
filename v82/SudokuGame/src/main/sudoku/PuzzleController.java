package main.sudoku;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.time.LocalTime;

public class PuzzleController {//this class used to manage all the things about puzzle maps including some control methods

	//most of these values would be saved in save files
	
	Integer[][] map1 = new Integer[9][9];//main map
	Integer[][] map2 = new Integer[9][9];//backup map
	Integer[][] solution1=new Integer[9][9];//after player give up we need to solve for them
	Integer[][] solution2=new Integer[9][9];
	boolean[][] puzzleholder=new boolean[9][9];//prevent player change the original puzzle

	///////Puzzle status variables//////(probably need reset every time and need to judge before most actions)
	boolean isGen; // to indicate if the map1 is generated completely(true->complete)
	boolean isBackupGen;//to indicate if the preload map2 and solution are generated completely (true->complete)
	//--------fail indicator
	boolean isFail=false;
	//--------complete indicator
	boolean isCurrentSolv; //this variable told when to show a result screen (indicate if player solved the puzzle, and wait for the player to press next)
	////////////////////////////////////
	
	
	//conflict detection
	//this array indicate where are the conflicts in map1
	boolean[][] conflict =new boolean[9][9];//provide conflict check for each number in puzzle controller .map1
	
	//--------game difficulty--0: easy; 1: normal; 2: hard
	int difficulty=-1;//if this ==-1 means that player havent select difficulty, indicate the difficulty of puzzle
	
	//--------solve records
	int bestRecord=0;
	int currentRecord=0;
	int bestRecordfor2=0;//for challenge mode
	int currentRecordfor2=0;
			
			
	//--------time indicator for challenge mode (timer control variables, need reset in challenge mode)
	int timer;
	int passedTime;
	
	//--------puzzle position in jframe---------
	int puzzleStartPosX;
	int puzzleEndPosX;
	int puzzleStartPosY;
	int puzzleEndPosY;
	int puzzleWidth;
	int puzzleHeight;
	int cellWidth;
	int cellHeight;
	//----------cell fade in control variables-----------
	int alphaFadeEffectForHoverCell=0;
	int alphaFadeEffectForSelectCell=0;
	int lasthx=0;int lasthy=0;//last hover position in cells
	int cellHoverFadeRate=4;
	int cellSelectionFadeRate=3;
	
	
	//to record cell has been selected by mouse, the selectors
	int selectCellX;
	int selectCellY;
	
	PuzzleGenV2 PG= new PuzzleGenV2();//puzzle generator
	MainSolvGen MSG=new MainSolvGen();//map generator
	
	Thread threadPC;
	
	
	
	
	
	public PuzzleController(int difficulty, int startX, int startY, int endX, int endY) {

		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				map1[i][j] = 0;
			}
		}
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				map2[i][j] = 0;
			}
		}
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				puzzleholder[i][j] = false;
			}
		}
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				conflict[i][j] = false;
			}
		}
		
		
		this.difficulty=difficulty;
		
		isCurrentSolv=false;
		
		isGen=false;
		isBackupGen=false;
		//isSolutionGen=false;
		
		bestRecord=0;//for standard
		currentRecord=0;
		bestRecordfor2=0;//for challenge mode
		currentRecordfor2=0;
		
		isFail=false;
		timer=-1;
		passedTime=0;
		
		selectCellX=-1;
		selectCellY=-1;
		
		//as best records is always keep increment, load it as default
		if(SaveLoad.fileExistSt())
		{
			SaveLoad sl=new SaveLoad();
			sl.loadBestRecordSt(this);
		}
		if(SaveLoad.fileExistCh())
		{
			SaveLoad sl=new SaveLoad();
			sl.loadBestRecordCh(this);
		}
		//this method is called only if player start a new game series, so current records will not load here
		
		puzzleStartPosX=startX;
		puzzleStartPosY=startY;
		puzzleEndPosX=endX;
		puzzleEndPosY=endY;
		
		puzzleWidth=((puzzleEndPosX-puzzleStartPosX));
		puzzleHeight=((puzzleEndPosY-puzzleStartPosY));
		
		cellWidth=puzzleWidth/9;
		cellHeight=puzzleHeight/9;
		
	}
	/////////////////those methods will control generation and set or reset of control variables////////////
	//those thread is temporarily and will ends after generating
	//this is for generate puzzle when first start the game (generation if no backup is available)
	//only call this if only right after the initialization of PuzzleController, as it only have generate function but not reset function
	public void initGenerationControl()
	{
		
		threadPC=new Thread(() ->  {
			
			// when first time enter the game generate both the first map and a backup map
			MSG.generator(map1, 0, 0);
			
			saveSolution1(map1);
			
			PG.puzzle(map1,difficulty);

			for (int i = 0; i < 9; i++)// hold the puzzle
			{
				for (int j = 0; j < 9; j++) {
					puzzleholder[i][j] = map1[i][j] == 0 ? false : true;
				}
			}

			isGen = true;

			MSG.generator(map2, 0, 0);
			
			saveSolution2(map2);
			
			PG.puzzle(map2,difficulty);
			
			//genSolution();

			isBackupGen = true;
		
			///////////////////////////////////////// =========================
			
			
		});//.start();
		threadPC.start();
	}
	
	///this thread is used to generate puzzle plus reset variable for next puzzle when pressed next button (generation when backup is prepared)
	public void contGenerationControl() {
		//this method would also reset some necessary variables
		isGen = false;
		isBackupGen = false;
		//isSolutionGen=false;
		isCurrentSolv=false;
		
		isFail=false;
		//reset timer and passedTime
		resetTimer();
		
		selectCellX=-1;
		selectCellY=-1;
		
		threadPC=new Thread(() ->  {
			
			
			for (int i = 0; i < 9; i++) {
				for (int j = 0; j < 9; j++) {
					map1[i][j] = map2[i][j].intValue();//give map2 to map1 and set up the puzzle
					puzzleholder[i][j] = map1[i][j] == 0 ? false : true;
				}
			}
			saveSolution1(solution2);

			isGen = true;

			// backup map generation (map2)
			for (int i = 0; i < 9; i++) {
				for (int j = 0; j < 9; j++) {
					map2[i][j] = 0;
				}
			}
			MSG.generator(map2, 0, 0);
			
			saveSolution2(map2);
			
			PG.puzzle(map2, difficulty);

			
			//genSolution();
			
			isBackupGen = true;
			

		
		});
		
		threadPC.start();
		
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	public void saveSolution1(Integer[][] map) {//generate a solution as well
		
					
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				
				
					solution1[i][j] = 0;
			}
		}
					
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				
				//if(puzzleholder[i][j])
					solution1[i][j] = map[i][j].intValue();
			}
		}

		//MSG.generator(solution, 0, 0);//
		
		
	}
	public void saveSolution2(Integer[][] map) {//generate a solution as well
		
		
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				
				
					solution2[i][j] = 0;
			}
		}
					
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				
				//if(puzzleholder[i][j])
					solution2[i][j] = map[i][j].intValue();
			}
		}

		//MSG.generator(solution, 0, 0);//
		
		
	}
	
	
	////////////below is completion check/////////////////////////////////////////////////////////////////////////////////////////////
	public void SetCompleteForLoading() {//standalone setcomplete for loading (when loaded puzzle already complete, do not increment any record)
		isCurrentSolv=checkComplete();//set isCurrentSolv to true if the map is completely solved
	}
	
	public void autoSetComplete() {//auto set solve state to true and increment records
		
		boolean complete=checkComplete();
		if(complete) //when player solved the puzzle
		{
			
			if(Game.gameStat==1 && !isCurrentSolv)//!isCurrentSolv is for limit the current record only add one time per completion
				currentRecord+=1;//when completely solve the puzzle the current record +1
			else if(!isCurrentSolv)
				currentRecordfor2+=1;//for challenge mode
			
			if(currentRecord>bestRecord) {//if current record exceed best record, best record = current
				bestRecord=currentRecord;
			}
			if(currentRecordfor2>bestRecordfor2) {//for challenge mode
				bestRecordfor2=currentRecordfor2;
			}
		}
		isCurrentSolv=complete;//set the complete indicator to current state
		
	}
	
	public boolean checkComplete()//check if the puzzle is completely solved
	{
		boolean c1= checkFilled();//because wee need to make sure run over each of those every fill
		boolean c2= !checkConflict();
		//System.out.println(c1+" "+c2+" "+isGen);
		return  c1 && c2 && isGen;
	}
	
	public boolean checkFilled()//check if map1 is filled
	{
		boolean thismapfilled=true;
		for(int i=0;i<9;i++)
		{
			for(int j=0;j<9;j++)
			{
				if(map1[i][j].intValue()==0)
				{
					thismapfilled=false;
				}
			}
		}
		return thismapfilled;
	}
	
	public boolean checkConflict()//check if there is conflict &  set the conflict number array
	{
		// reset the conflict indicator and test it again per frame
		boolean hasConflict=false;//
		for(int i=0;i<9;i++)
		{
			for(int j=0;j<9;j++)
			{
				conflict[i][j]=false;
			}
		}
		
		//test for conflict && label where are the conflicts(update every frame)
		for(int i=0;i<9;i++)
		{
			for(int j=0;j<9;j++)
			{
				if(map1[i][j]!=0)
				{
					for(int c=0;c<9;c++)
					{
						if(c!=j && map1[i][c]==map1[i][j])
						{
							hasConflict=true;
							conflict[i][j]=true;
							conflict[i][c]=true;
						}
					}
					for(int c=0;c<9;c++)
					{
						if(c!=i && map1[c][j]==map1[i][j])
						{
							hasConflict=true;
							conflict[i][j]=true;
							conflict[c][j]=true;
						}
					}
					for(int cx=(i/3)*3;cx<(i/3)*3+3;cx++)
					{
						for(int cy=(j/3)*3;cy<(j/3)*3+3;cy++)
						{
							if(cx!=i && cy!=j && map1[cx][cy]==map1[i][j])
							{
								hasConflict=true;
								conflict[i][j]=true;
								conflict[cx][cy]=true;
							}
						}
					}
				}
			}
		}
		
		return hasConflict;
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////	
	///////////timer check (use when needed) ////////////////////////////////////////////
	public void setTimer()//this method will auto set timer only if no one set it before(timer=-1) and if the puzzle is generated completely
	{
		if(timer == -1)//timer=-1 indicate that the timer has not started yet
		{
			//timer=LocalTime.now().getSecond()+LocalTime.now().getMinute()*60+LocalTime.now().getHour()*60*60;//timer now record the initial time when generation is completed, the duration will be calculated by using this time
			timer=0;
		}
	}
	public void setPassedTime() {//this method will record passedtime only if timer started, and current game has not ended
		if(timer != -1)// && !isCurrentSolv && !isFail)
		{
			timer++;
			if(timer>=60)///every frame plus one, as 60fps 60 times mean 1 sec
			{
				passedTime+=1;//LocalTime.now().getSecond()+LocalTime.now().getMinute()*60+LocalTime.now().getHour()*60*60-timer;
				timer=0;
			}
		}
	}
	//////////////////////////////////////////////////////////////////
	////////failure judgement ///////////////////////////////////
	public void autoSetfailedTime()//auto set isFail to true when time passed, will also return current state of isFail
	{//the preconditions are the game havent end already, the timer is set to started
		
		setTimer();
		setPassedTime();
		if(timer!=-1 && passedTime>=1000)// &&!isFail)//time indicator-----------------------
		{												//when time is up, the game is set to failed
			isFail=true;//if exceeds the time limit failed
			currentRecordfor2=0;//current record will be reset in time mode when failed
		}
		//return isFail;
	}
	////////////manually set fail///////////////////////
	public void setFail()//general method for set game state to fail
	{
		
		isFail=true;
		currentRecord=0;
		currentRecordfor2=0;
		
	}
	//////////////////////////////////////////////////////
	////////////////clear///////////
	public void clearMap1()
	{
		for(int i=0;i<9;i++)
		{
			for(int j=0;j<9;j++)
			{
				if(!puzzleholder[i][j])
				{
					map1[i][j]=0;
				}
			}
		}
	}
	////////////////////////////////
	///////////delete///////////////
	public void deleteSelectCell()//delete the number in the cell when selected by selectors //used by delete button//can only delete player input numbers
	{
		if(this.selectCellX!=-1 && this.selectCellY!=-1 && !puzzleholder[this.selectCellX][this.selectCellY])
			map1[this.selectCellX][this.selectCellY]=0;
	}
	////////////////////////////////
	/////////////reset all (just in case)//////////////////
	public void resetAll() {
		
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				map1[i][j] = 0;
			}
		}
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				map2[i][j] = 0;
			}
		}
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				puzzleholder[i][j] = false;
			}
		}
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				conflict[i][j] = false;
			}
		}
		
		
		difficulty=-1;
		
		isCurrentSolv=false;
		
		isGen=false;
		isBackupGen=false;
		
		bestRecord=0;//for standard
		currentRecord=0;
		bestRecordfor2=0;//for challenge mode
		currentRecordfor2=0;
		
		isFail=false;
		timer=-1;
		passedTime=0;
		
		if(SaveLoad.fileExistSt())
		{
			SaveLoad sl=new SaveLoad();
			sl.loadBestRecordSt(this);
		}
		if(SaveLoad.fileExistCh())
		{
			SaveLoad sl=new SaveLoad();
			sl.loadBestRecordCh(this);
		}
	}
	
	public void resetTimer() {//reset timer and passedTime
		timer=-1;
		passedTime=0;
	}
	//////////////////////////////////////////////////////////////////
	
	//20-380 for x, 20-380 for y
	//keyboard input and mouse selection change
	public void puzzleInput(int mousex,int mousey, int keynumber, boolean isMouseDown) {
		//type in the numbers in selected cells===when the mouse has clicked in this area, uses the last mouse position to get which cell selected
		if(puzzleStartPosX<=mousex && mousex<puzzleEndPosX && puzzleStartPosY<=mousey && mousey<puzzleEndPosY && isMouseDown)//when mouse click in a cell change selectors
		{
			this.selectCellX=(mousex-20)/40;//get which cell is selected or said update the selected cell;
			this.selectCellY=(mousey-20)/40;
		}

		if(0<=selectCellX&&selectCellX<=8 && 0<=selectCellY&&selectCellY<=8)//just in case
		{	
			if((keynumber==KeyEvent.VK_1 ||keynumber==KeyEvent.VK_2||keynumber==KeyEvent.VK_3||
					keynumber==KeyEvent.VK_4||keynumber==KeyEvent.VK_5||keynumber==KeyEvent.VK_6||
					keynumber==KeyEvent.VK_7||keynumber==KeyEvent.VK_8||keynumber==KeyEvent.VK_9) && !puzzleholder[selectCellX][selectCellY])//49-57 check if player press the keyboard in right keys, and check if this select area is legal to be changed
			{
				map1[this.selectCellX][this.selectCellY]=keynumber-48;//input number in this cell
				
				
			}
			else if((keynumber==KeyEvent.VK_BACK_SPACE ||keynumber==KeyEvent.VK_SPACE) && !puzzleholder[selectCellX][selectCellY])//delete the number in the cells by press backspace or space
			{
				map1[selectCellX][selectCellY]=0;//delete
			}
			else if(keynumber==KeyEvent.VK_UP && selectCellY-1>=0)//key up
			{
				selectCellY-=1;

			}
			else if(keynumber==KeyEvent.VK_DOWN && selectCellY+1<=8)//key down
			{
				selectCellY+=1;

			}
			else if(keynumber==KeyEvent.VK_LEFT && selectCellX-1>=0)//key left
			{
				selectCellX-=1;

			}
			else if(keynumber==KeyEvent.VK_RIGHT && selectCellX+1<=8)//key right
			{
				selectCellX+=1;

			}
			//keynumber=-1;
			
		}
		//else//no number input when no cell is selected
			//	keynumber=-1;
	}
	
	//render puzzle mouse action and selections
	public void renderPuzzleInputLabel(int mousex,int mousey, int mousehx,int mousehy,boolean isMouseDown, Graphics g) 
	{
		////////////////////////////cells///////////////////////////////////////////
		int currentSelectRenderX=selectCellX*cellWidth+puzzleStartPosX;
		int currentSelectRenderY=selectCellY*cellHeight+puzzleStartPosY;
		//this part is for mouse hover in cells
		if(puzzleStartPosX<=mousehx && mousehx<puzzleEndPosX && puzzleStartPosY<=mousehy && mousehy<puzzleEndPosY)// && (!isFail && !isCurrentSolv) && isGen)
		{
			int xhx=(mousehx-puzzleStartPosX)/(cellWidth);// /40
			int yhy=(mousehy-puzzleStartPosY)/(cellHeight);
			//-----------fade effect------
			if(xhx!=lasthx || yhy!=lasthy)
			{
				alphaFadeEffectForHoverCell=0;
				lasthx=xhx;
				lasthy=yhy;
			}
			if(alphaFadeEffectForHoverCell<=255-cellHoverFadeRate)
				alphaFadeEffectForHoverCell+=cellHoverFadeRate;//g.setColor(new Color(50,50,50,fadeEffectForHoverCell+=4));
			else
				alphaFadeEffectForHoverCell=255;//g.setColor(new Color(50,50,50));
			//------------------------------------------
			g.setColor(new Color(50,50,50,alphaFadeEffectForHoverCell));
			g.fillRect((cellWidth)*xhx+puzzleStartPosX, (cellHeight)*yhy+puzzleStartPosY, (cellWidth), (cellHeight));
			
		
		
		}
		else
			alphaFadeEffectForHoverCell=0;
		
		//print the mouse selected cell when mouse click in cells, and whatever failed or solved, the cells will be unable to select
		if(puzzleStartPosX<=currentSelectRenderX && currentSelectRenderX<puzzleEndPosX && puzzleStartPosY<=currentSelectRenderY && currentSelectRenderY<puzzleEndPosY)// && (!isFail && !isCurrentSolv) && isGen)
		{
			//----fade--------
			//when mouse is pressed in this cell
			if(isMouseDown && (puzzleStartPosX<=mousex && mousex<puzzleEndPosX && puzzleStartPosY<=mousey && mousey<puzzleEndPosY))
				alphaFadeEffectForSelectCell=0;
			if(alphaFadeEffectForSelectCell<=255-cellSelectionFadeRate)
				alphaFadeEffectForSelectCell+=cellSelectionFadeRate;
			else
				alphaFadeEffectForSelectCell=255;
			//-----------------
			if(map1[selectCellX][selectCellY]==0)//highlight the cell selected
			{
				g.setColor(new Color(0,255,0,alphaFadeEffectForSelectCell));
				g.drawRect(cellWidth*selectCellX+puzzleStartPosX, cellHeight*selectCellY+puzzleStartPosY, cellWidth, cellHeight);
				g.fillRect(cellWidth*selectCellX+puzzleStartPosX, cellHeight*selectCellY+puzzleStartPosY, cellWidth, cellHeight);
			}
			if(map1[selectCellX][selectCellY]!=0 && !puzzleholder[selectCellX][selectCellY])//highlight the cell selected
			{
				g.setColor(new Color(0,255,255,alphaFadeEffectForSelectCell));
				g.drawRect(cellWidth*selectCellX+puzzleStartPosX, cellHeight*selectCellY+puzzleStartPosY, cellWidth, cellHeight);
				g.fillRect(cellWidth*selectCellX+puzzleStartPosX, cellHeight*selectCellY+puzzleStartPosY, cellWidth, cellHeight);
			}
			
			if(map1[selectCellX][selectCellY]!=0 && puzzleholder[selectCellX][selectCellY])//if there is an original puzzle number use a different way to highlight
			{
				g.setColor(new Color(0,255,255,alphaFadeEffectForSelectCell));
				g.drawRect(cellWidth*selectCellX+puzzleStartPosX, cellHeight*selectCellY+puzzleStartPosY, cellWidth, cellHeight);
				g.fillRect(cellWidth*selectCellX+puzzleStartPosX, cellHeight*selectCellY+puzzleStartPosY, cellWidth, cellHeight);
				g.setColor(new Color(0,0,0,alphaFadeEffectForSelectCell));
				g.fillRect(cellWidth*selectCellX+puzzleStartPosX+3, cellHeight*selectCellY+puzzleStartPosY+3, cellWidth-6, cellHeight-6);
			}
			
		
		}
	}
	//render puzzle cells
	public void renderPuzzle(Graphics g)
	{
		//print out the background sheet///
		for(int i=1;i<9;i++)
		{
			if(i%3==0)
				g.setColor(new Color(255,255,255));
			else
				g.setColor(new Color(100,100,100));
			g.drawLine(puzzleStartPosX+cellWidth*i, puzzleStartPosY, puzzleStartPosX+cellWidth*i, puzzleEndPosY);
		}
		for(int i=0;i<10;i++)
		{
			if(i%3==0)
				g.setColor(new Color(255,255,255));
			else
				g.setColor(new Color(100,100,100));
			g.drawLine(puzzleStartPosX, puzzleStartPosY+cellHeight*i, puzzleEndPosX, puzzleStartPosY+cellHeight*i);
		}
		g.setColor(new Color(255,255,255));
		g.drawLine(puzzleStartPosX+cellWidth*0, puzzleStartPosY, puzzleStartPosX+cellWidth*0, puzzleEndPosY);
		g.drawLine(puzzleStartPosX+cellWidth*9, puzzleStartPosY, puzzleStartPosX+cellWidth*9, puzzleEndPosY);
		
		//print out the puzzle && filled in numbers (map1 array)/////////
		
		if(isGen)//when generation complete
		{
			g.setFont(new Font("TimesRoman", Font.PLAIN, 40));
			g.setColor(new Color(255,255,255));
			for(int i=0;i<9;i++)
			{
				for(int j=0;j<9;j++)
				{
					
					//for centered align
					int fWidth = g.getFontMetrics().stringWidth(Integer.toString(map1[i][j].intValue()));
					int fHeight = g.getFontMetrics().getAscent()-g.getFontMetrics().getDescent();//g.getFontMetrics().getHeight()-g.getFontMetrics().getLeading();//-g.getFontMetrics().getAscent()-g.getFontMetrics().getDescent();
					//g.getFontMetrics().;
					int reX=Math.abs(this.cellWidth- fWidth)/2; 
					int reY=Math.abs(this.cellHeight - fHeight)/2 ;
					
					
					
					if(conflict[i][j]==false)//when there is no conflict in this cell
					{
						
						if(map1[i][j].intValue()==0)
							g.drawString(" ", puzzleStartPosX+reX+i*cellWidth, puzzleStartPosY+j*cellHeight-reY+cellHeight);
						else if(puzzleholder[i][j])
						{
							g.setColor(new Color(255,255,255));
							g.drawString(Integer.toString(map1[i][j].intValue()), puzzleStartPosX+reX+i*cellWidth, puzzleStartPosY+j*cellHeight-reY+cellHeight);
						}
						else
						{
							g.setColor(new Color(100,100,100));
							g.drawString(Integer.toString(map1[i][j].intValue()), puzzleStartPosX+reX+i*cellWidth, puzzleStartPosY+j*cellHeight-reY+cellHeight);
						}
					}
					else//when there is conflict in this cell, use a different color to print
					{
						if(puzzleholder[i][j])
						{
							g.setColor(new Color(255,0,0));
							g.drawString(Integer.toString(map1[i][j].intValue()), puzzleStartPosX+reX+i*cellWidth, puzzleStartPosY+j*cellHeight-reY+cellHeight);
						}
						else
						{
							g.setColor(new Color(100,0,0));
							g.drawString(Integer.toString(map1[i][j].intValue()), puzzleStartPosX+reX+i*cellWidth, puzzleStartPosY+j*cellHeight-reY+cellHeight);
						}
					}
				}
			
			}
		}
		else {//when generation is not complete, show the text of loading and indicate the progress
			
			
			//g.setColor(new Color(50,50,50));
			//g.drawRect(20, 20, 360, 360);g.fillRect(20, 20, 360, 360);
			
			g.setColor(new Color(100,100,100,150));
			//g.drawRect(40*PC.PG.px+20, 40*PC.PG.py+20, 40, 40);g.fillRect(40*PC.PG.px+20, 40*PC.PG.py+20, 40, 40);
			g.fillRect(puzzleStartPosX, puzzleStartPosY, (int)((PG.progress)*puzzleWidth), puzzleHeight);
		}
		///////////////////////////////////////cells end/////////////////////////////////////////////////
	}

	
	public void renderRecords(double gameStat,int x, int y, int w, int h,Graphics g) {
		g.setColor(new Color(255,255,255));
		g.drawRect(x, y, w, h);
		g.setFont(new Font("TimesRoman", Font.PLAIN, 40));
		g.setColor(new Color(255,255,255));
		g.drawString("Records:", x+10, y+40);
		if(gameStat==1)
		{
			g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
			g.setColor(new Color(255,255,255));
			g.drawString("Best: "+bestRecord, x+10, y+40+45);
			g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
			g.setColor(new Color(255,255,255));
			g.drawString("Current: "+currentRecord, x+10, y+40+85);
		}
		else
		{
			g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
			g.setColor(new Color(255,255,255));
			g.drawString("Best: "+bestRecordfor2, x+10, y+40+45);
			g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
			g.setColor(new Color(255,255,255));
			g.drawString("Current: "+currentRecordfor2, x+10, y+40+85);
		}
	}

	public void renderDifficulty(double gameStat,int x, int y,Graphics g)
	{
		g.setColor(new Color(255,255,255));
		if(difficulty==0 && gameStat==2)
		{
			g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
			g.drawString("Easy Challenge", 20, 380+40);
		}
		else if(difficulty==1 && gameStat==2)
		{
			g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
			g.drawString("Normal Challenge", 20, 380+40);
		}
		else if(difficulty==2 && gameStat==2)
		{
			g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
			g.drawString("Hard Challenge", 20, 380+40);
		}
		else if(difficulty==0)
		{
			g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
			g.drawString("Easy Standard", 20, 380+40);
		}
		else if(difficulty==1)
		{
			g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
			g.drawString("Normal Standard", 20, 380+40);
		}
		else if(difficulty==2)
		{
			g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
			g.drawString("Hard Standard", 20, 380+40);
		} 
	}

	public void renderTimer(double gameStat, int x, int y,Graphics g)
	{
		if(gameStat==2 && timer!=-1)// && !isFail && !isCurrentSolv && timer!=-1 && isGen)
		{
			
			g.setColor(new Color(255,255,255));
			
			g.setFont(new Font("TimesRoman", Font.PLAIN, 20));
			g.drawString("Time Left:", x, y);
			
			int fWidth = g.getFontMetrics().stringWidth("Time Left:");
			
			g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
			g.drawString(Integer.toString(1000-(passedTime))+" s", x+fWidth+10, y);
		}
		if(gameStat==1)// &&!isFail&& !isCurrentSolv && isGen)
		{
			g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
			g.setColor(new Color(255,255,255));
			
			g.setFont(new Font("TimesRoman", Font.PLAIN, 20));
			g.drawString("Clock:", x, y);
			
			int fWidth = g.getFontMetrics().stringWidth("Clock:");
			
			g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
			g.drawString(LocalTime.now().getHour()+":"+LocalTime.now().getMinute()+":"+LocalTime.now().getSecond(), x+fWidth+10, y);
		}
	}

	public void renderPuzzleStatus(int x, int y, Graphics g)
	{
		if(!isGen)
		{
			g.setFont(new Font("TimesRoman", Font.PLAIN, 40));
			g.setColor(new Color(255,255,255));
			g.drawString("Loading", x, y);
		}
		
		if(isFail)//if failed ( only in challenge mode player could failed)
		{
			g.setFont(new Font("TimesRoman", Font.PLAIN, 40));
			g.setColor(new Color(255,255,255));
			g.drawString("Failed!", x, y);
		}
		else if(isCurrentSolv)//if completed
		{
			g.setFont(new Font("TimesRoman", Font.PLAIN, 40));
			g.setColor(new Color(255,255,255));
			g.drawString("Completed!", x, y);
		}
	}
	
	public void renderSolution(Graphics g)//render solution at puzzle location
	{
	
		g.setFont(new Font("TimesRoman", Font.PLAIN, 40));
		g.setColor(new Color(255,255,255));
		
		
		for(int i=0;i<9;i++)
		{
			for(int j=0;j<9;j++)
			{
				//for centered align
				int fWidth = g.getFontMetrics().stringWidth(Integer.toString(solution1[i][j].intValue()));
				int fHeight = g.getFontMetrics().getAscent()-g.getFontMetrics().getDescent();//g.getFontMetrics().getHeight()-g.getFontMetrics().getLeading();//-g.getFontMetrics().getAscent()-g.getFontMetrics().getDescent();
			
				int reX=Math.abs(this.cellWidth- fWidth)/2; 
				int reY=Math.abs(this.cellHeight - fHeight)/2 ;
				
				
				
				if(map1[i][j].intValue()==solution1[i][j].intValue())//the answer is same to result
				{
					if(puzzleholder[i][j])
					{
						g.setColor(new Color(255,255,255));
						g.drawString(Integer.toString(solution1[i][j].intValue()), puzzleStartPosX+reX+i*cellWidth, puzzleStartPosY+j*cellHeight-reY+cellHeight);
					}
					else
					{
						g.setColor(new Color(100,150,100));//use green to indicate the correct answer numbers
						g.drawString(Integer.toString(solution1[i][j].intValue()), puzzleStartPosX+reX+i*cellWidth, puzzleStartPosY+j*cellHeight-reY+cellHeight);
					}
				}
				else//the answer is different to result
				{
					if(map1[i][j]==0)
					{
						g.setColor(new Color(100,100,150));
						g.drawString(Integer.toString(solution1[i][j].intValue()), puzzleStartPosX+reX+i*cellWidth, puzzleStartPosY+j*cellHeight-reY+cellHeight);
					}
					else
					{
						
						g.setColor(new Color(100,100,150));
						g.drawString(Integer.toString(solution1[i][j].intValue()), puzzleStartPosX+reX+i*cellWidth, puzzleStartPosY+j*cellHeight-reY+cellHeight);
					}
					
				}
			}
		
		}
		
		//print out the background sheet///
		for(int i=1;i<9;i++)
		{
			if(i%3==0)
				g.setColor(new Color(255,255,255));
			else
				g.setColor(new Color(100,100,100));
			g.drawLine(puzzleStartPosX+cellWidth*i, puzzleStartPosY, puzzleStartPosX+cellWidth*i, puzzleEndPosY);
		}
		for(int i=0;i<10;i++)
		{
			if(i%3==0)
				g.setColor(new Color(255,255,255));
			else
				g.setColor(new Color(100,100,100));
			g.drawLine(puzzleStartPosX, puzzleStartPosY+cellHeight*i, puzzleEndPosX, puzzleStartPosY+cellHeight*i);
		}
		g.setColor(new Color(255,255,255));
		g.drawLine(puzzleStartPosX+cellWidth*0, puzzleStartPosY, puzzleStartPosX+cellWidth*0, puzzleEndPosY);
		g.drawLine(puzzleStartPosX+cellWidth*9, puzzleStartPosY, puzzleStartPosX+cellWidth*9, puzzleEndPosY);
		
			
		
	}
}
