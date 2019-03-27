package main.sudoku;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.time.LocalTime;
import java.util.Scanner;

public class SaveLoad {//this class is used for load and save the data in a puzzlecontroller based on game modes

	//Thread threadSL;
	private int key1=8191;
	private int key2=6987;
	
	public SaveLoad() {
		
	}
	
	public void save(PuzzleController PC, double gameStat) {//save function for all modes
		//threadSL = new Thread(()-> {
			
			File sfile=new File("SaveST.sudoku");
			File sfile2=new File("SaveCH.sudoku");
			
			try {
				
				
				
				if(gameStat==1)
				{
					PrintWriter pw=new PrintWriter(new FileWriter(sfile,false));
					pw.println(PC.bestRecord);
					pw.println(PC.currentRecord);
					
					pw.println(PC.difficulty);
					pw.println(PC.isFail?1:0);
					
					for(int i=0;i<9;i++)
					{
						for(int j=0;j<9;j++)
						{
							pw.print(PC.map1[i][j]+" ");
						}
						pw.println();
					}
					
					for(int i=0;i<9;i++)
					{
						for(int j=0;j<9;j++)
						{
							pw.print((PC.puzzleholder[i][j]?1:0)+" ");
						}
						pw.println();
					}
					
					for(int i=0;i<9;i++)
					{
						for(int j=0;j<9;j++)
						{
							pw.print(PC.map2[i][j]+" ");
						}
						pw.println();
					}
					
					for(int i=0;i<9;i++)
					{
						for(int j=0;j<9;j++)
						{
							pw.println((PC.solution1[i][j]*key1+i+j)*key1);
						}
					}
					for(int i=0;i<9;i++)
					{
						for(int j=0;j<9;j++)
						{
							pw.println((PC.solution2[i][j]*key2+i+j)*key2);
						}
					}
					
					pw.close();
					
				}
				else if(gameStat==2)
				{
					PrintWriter pw=new PrintWriter(new FileWriter(sfile2,false));
					
					pw.println(PC.bestRecordfor2);
					pw.println(PC.currentRecordfor2);
					pw.println(PC.difficulty);
					//pw.println(Game.timer);
					pw.println(PC.passedTime);
					pw.println(PC.isFail?1:0);
					
					for(int i=0;i<9;i++)
					{
						for(int j=0;j<9;j++)
						{
							pw.print(PC.map1[i][j]+" ");
						}
						pw.println();
					}
					
					for(int i=0;i<9;i++)
					{
						for(int j=0;j<9;j++)
						{
							pw.print((PC.puzzleholder[i][j]?1:0)+" ");
						}
						pw.println();
					}
					
					for(int i=0;i<9;i++)
					{
						for(int j=0;j<9;j++)
						{
							pw.print(PC.map2[i][j]+" ");
						}
						pw.println();
					}
					
					for(int i=0;i<9;i++)
					{
						for(int j=0;j<9;j++)
						{
							pw.println((PC.solution1[i][j]*key1+i+j)*key1);
						}
					}
					for(int i=0;i<9;i++)
					{
						for(int j=0;j<9;j++)
						{
							pw.println((PC.solution2[i][j]*key2+i+j)*key2);
						}
					}
					pw.close();
				}		
				
				
				
				
				System.out.println("Saved");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("eeerrrorr");
			}
			
			
		//});

	
	}
	
	
	public void loadSt(PuzzleController PC)//load file for standard mode
	{
		File rfile=new File("SaveST.sudoku");
		//File rfile2=new File("SaveCH.sudoku");
		
		try {
			
			Scanner sc=new Scanner(rfile);
			
			PC.bestRecord=sc.nextInt();
			PC.currentRecord=sc.nextInt();//as this method use to continue the game, continue not count as a new game, so the current record will be recovered
			PC.difficulty=sc.nextInt();
			PC.isFail=sc.nextInt()==1?true:false;
			
			for(int i=0;i<9;i++)
			{
				for(int j=0;j<9;j++)
				{
					PC.map1[i][j]=sc.nextInt();
				}
			}
			for(int i=0;i<9;i++)
			{
				for(int j=0;j<9;j++)
				{
					int temp=sc.nextInt();
					PC.puzzleholder[i][j]=temp==1?true:false;
				}
			}
			for(int i=0;i<9;i++)
			{
				for(int j=0;j<9;j++)
				{
					PC.map2[i][j]=sc.nextInt();
				}
			}
			
			//PC.genSolutionInNewThread();//this method will set isbackupgen to true after done
			for(int i=0;i<9;i++)
			{
				for(int j=0;j<9;j++)
				{
					PC.solution1[i][j]=(sc.nextInt()/key1-i-j)/key1;
				}
			}
			for(int i=0;i<9;i++)
			{
				for(int j=0;j<9;j++)
				{
					PC.solution2[i][j]=(sc.nextInt()/key2-i-j)/key2;
				}
			}
			
			PC.isGen=true;
			PC.isBackupGen=true;
			
		}catch(Exception e)
		{
			System.out.println("eeerrror st load");
		}
		
	}
	public void loadCh(PuzzleController PC)//load for challenge mode
	{
		File rfile=new File("SaveCH.sudoku");
		//File rfile2=new File("SaveCH.sudoku");
		
		try {
			
			Scanner sc=new Scanner(rfile);
			
			PC.bestRecordfor2=sc.nextInt();
			PC.currentRecordfor2=sc.nextInt();//as this method use to continue the game, continue not count as a new game, so the current record will be recovered
			PC.difficulty=sc.nextInt();
			//Game.timer=sc.nextInt();
			PC.passedTime=sc.nextInt();
			PC.timer=0;//LocalTime.now().getSecond()+LocalTime.now().getMinute()*60+LocalTime.now().getHour()*60*60-Game.PC.passedTime;
			PC.isFail=sc.nextInt()==1?true:false;
			
			for(int i=0;i<9;i++)
			{
				for(int j=0;j<9;j++)
				{
					PC.map1[i][j]=sc.nextInt();
				}
			}
			for(int i=0;i<9;i++)
			{
				for(int j=0;j<9;j++)
				{
					int temp=sc.nextInt();
					PC.puzzleholder[i][j]=temp==1?true:false;
				}
			}
			
			for(int i=0;i<9;i++)
			{
				for(int j=0;j<9;j++)
				{
					PC.map2[i][j]=sc.nextInt();
				}
			}
			
			//PC.genSolutionInNewThread();////this method will set isbackupgen to true after done
			for(int i=0;i<9;i++)
			{
				for(int j=0;j<9;j++)
				{
					PC.solution1[i][j]=(sc.nextInt()/key1-i-j)/key1;
				}
			}
			for(int i=0;i<9;i++)
			{
				for(int j=0;j<9;j++)
				{
					PC.solution2[i][j]=(sc.nextInt()/key2-i-j)/key2;
				}
			}
			
			PC.isGen=true;
			PC.isBackupGen=true;
		}catch(Exception e)
		{
			
		}
	}
	//BTW load means all puzzle maps and backup maps are generated already
	
	public void loadBestRecordSt(PuzzleController PC) {
		File rfile=new File("SaveST.sudoku");
		try {
			Scanner sc=new Scanner(rfile);
			
			PC.bestRecord=sc.nextInt();
			
		}catch(Exception e)
		{
			
		}
	}
	public void loadBestRecordCh(PuzzleController PC) {
		File rfile=new File("SaveCH.sudoku");
		try {
			Scanner sc=new Scanner(rfile);
			
			PC.bestRecordfor2=sc.nextInt();
			
		}catch(Exception e)
		{
			
		}
	}
	
	
	public static boolean fileExistSt() {//exist detection
		File file=new File("./saveST.sudoku");
		return file.exists();
	}
	public static boolean fileExistCh() {//exist detection
		File file=new File("./saveCH.sudoku");
		return file.exists();
	}
}
