package main.sudoku;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class PuzzleGenV2 {//this class is used for generate puzzle based on a complete map
	//to show where the calculating is
	int px=0;
	int py=0;
	
	//this is counter to control loop
	int counter;
	
	int totalCount;//this is the variable used to calculate the progress
	double progress;//progress percentage
	
	LoopSolv LS=new LoopSolv();
	
	boolean stop;//this variable used to force stop the calculation progress

	public PuzzleGenV2() {
		stop=false;
	}
	//////////////=================================
	
	
	
	public void puzzle(Integer[][] map,int diff)
	{
		px=0;py=0;
		progress=0;
		totalCount=0;
		Numbers[][] cand =new Numbers[9][9];
		
		for(int i=0;i<9;i++)
		{
			for(int j=0;j<9;j++)
			{
				cand[i][j]=new Numbers();//initial state of candidate records, candid for every cell default be 1 to 9 numbers. however, the candid only enable while the same cell in map is 0
			}
		}
		
		
		//first level naked single=========================================================================================
		counter=0;
		if(diff>=0 && diff!=2)//if the difficulty indicate that the need to generate a map with equal or higher difficulty, enter this level of generation
		{
			
			while(counter!=1000)//!Arrays.equals(forcompare, map))//first level of removal
			{
				if(this.stop) break;//stop calculation when asked stop
				
				counter++;
				totalCount+=1;
				if(diff==0) progress=totalCount/1000;
				else progress=(double)totalCount/2000;
				
				int rX=(int) (Math.random()*9);
				int rY=(int) (Math.random()*9);
				//System.out.println(rX+" "+rY);
				if(map[rX][rY]==0) continue;
				
				if(map[rX][rY]!=0)//scan through the xylines and blocks to see if there is other candid exist if we remove this cell
				{
					//remove the number to test if it is good to remove, if not put back
					Integer numberAt=map[rX][rY].intValue();
					map[rX][rY]=0;
					CandidScanner.scanner(map, cand);//
					////decide if remove
					if(cand[rX][rY].N.size()==1)//if the condition is good (there is only one candidate for this cell)
					{
						//keep the removal and move on
						
					}
					else
					{
						//else reverse the removal back to the original number
						map[rX][rY]=numberAt.intValue();
						CandidScanner.scanner(map, cand);
					
					}
					
				}
				
				
			}
		}
		
		///////////////////////
//		for(int i=0;i<9;i++)
//		{
//			for(int j=0;j<9;j++)
//			{
//				System.out.print(map[i][j]+" ");
//			}
//			System.out.println();
//		}
		////////////////////////////////
		
		
		
		
		CandidScanner.scanner(map, cand);
		//second level hidden single
		counter=0;
		if(diff>=1 && diff!=2)//if the difficulty indicate that the need to generate a map with equal or higher difficulty, enter this level of generation
		{
			while(counter!=1000)//!Arrays.equals(forcompare, map))//second level of removal
			{
				if(this.stop) break;//stop calculation when asked stop
				
				counter++;
				totalCount+=1;
				progress=(double)totalCount/2000;
				
				int rX=(int) (Math.random()*9);
				int rY=(int) (Math.random()*9);
				
				if(map[rX][rY]==0) continue;
				
				if(map[rX][rY]!=0)
				{
					///////////////////////
					Integer numberAt=map[rX][rY].intValue();//record the original number in the cell, and test if remove the number will gives other related lines the same candid number
					map[rX][rY]=0;//remove the number in order to test
					
					CandidScanner.scanner(map, cand);//update the candid numbers in order to test
					
					//check if the removed numbers still follow the rules====
					boolean isOky=true;
					boolean isOkx=true;
					
					//======================================================
					
					//check if this removal follow the rules
					isOky=true;
					isOkx=true;
					for(int c=0;c<9;c++)//if there is a line which does not have a same candid number of this cell in rest of cells, the condition is good, this number in this cell can be removed
					{
						
						if(map[rX][c]==0 && c!=rY && cand[rX][c].N.contains(numberAt))//in this case, not good
						{
							isOky=false;
							
						}
						if(map[c][rY]==0 && c!=rX && cand[c][rY].N.contains(numberAt))//in this case, not good
						{
							isOkx=false;
						}
					}
					
					
					if(!isOky && !isOkx)//if both condition is not good recover the cell and the candid for all the cells
					{
						map[rX][rY]=numberAt.intValue();
						CandidScanner.scanner(map, cand);
					}
					
					if(map[rX][rY]==0) //print the removed numbers and their positions if the cell is good to remove
					{
						//System.out.println("x: "+isOkx+" y: "+isOky);
						
						
					}
					
					
					/////////////////
					
				}
			}
		}
		/*
		///level 3 extra removal //naked pair
		//counter=0;
		if(diff>=2)//if the difficulty indicate that the need to generate a map with equal or higher difficulty, enter this level of generation
		{
			
			for(int rX=0;rX<9;rX++)//in this level of removal, we go through every cells left with values, and check if remove the values will keep the puzzle in single-solution status. 
			{
				if(this.stop) break;//as this part will spend a reasonable long period to be calculated, if the thread is asked to stop, stop continuing calculation
				for(int rY=0;rY<9;rY++)
				{
					if(this.stop) break;//as this part will spend a reasonable long period to be calculated, if the thread is asked to stop, stop continuing calculation
					
					px=rX;//this is for indicate generation progress
					py=rY;
					
					if(map[rX][rY]==0) continue;
					
					if(map[rX][rY]!=0)//if there still number left in the cell, we test it
					{
						Integer numberAt=map[rX][rY].intValue();// give the number to temp integer for possible later recover
						map[rX][rY]=0;//remove the value of the current cell in order to test it 
						CandidScanner.scanner(map, cand);//update the candid
						////
						
						
						boolean isSame=true;//this value indicate if the solutions are different or not
						for(int tt=0;tt<1;tt++)//this loop try to reduce the possibility of random solutions be same. however, we don't need it anymore
						{
							Integer[][] t1map =new Integer[9][9];//those arrays are temp array to compare the solutions
							Integer[][] t2map =new Integer[9][9];
							
							for(int i=0;i<9;i++)
							{
								for(int j=0;j<9;j++)
								{
									t1map[i][j]=map[i][j].intValue();//transfer the values of map into comparison arrays
									t2map[i][j]=map[i][j].intValue();
								}
							}
							
							LS.generatorLeft(t1map, 0, 0);//solve the comparison arrays by using two different solver
							LS.generatorRight(t2map, 0, 0);
							
							
							for(int i=0;i<9;i++)
							{
								for(int j=0;j<9;j++)
								{
									if(t1map[i][j]!=t2map[i][j])//compare if the comparison arrays are same, is they are not, there should be multi-solutions
										isSame=false;
								}
							}
							if(isSame ==false) break;
							
						}
						
						if(!isSame)//if there are multi-solutions recover the value of cells and moving on
						{
							map[rX][rY]=numberAt.intValue();
							CandidScanner.scanner(map, cand);
							//System.out.println("remove failed "+rX+" "+rY);
						}
						else//if there only is one solution keep the cell with removed status and moving on
							;//System.out.println("remove successful "+rX+" "+rY);
						
					}
				}
			}
		}
		
		px=0;
		py=0;
		*/
		
		counter=0;
		if(diff>=2)//if the difficulty indicate that the need to generate a map with equal or higher difficulty, enter this level of generation
		{
			ArrayList<Integer> rList=new ArrayList<Integer>();
			for(int i=0;i<81;i++)
				rList.add(i);
			Collections.shuffle(rList);
			
			while(counter<=80)
			{
				
				int rX=(int)((double)rList.get(counter)/9);
				int rY=rList.get(counter)%9;
				
				counter++;
				totalCount+=1;
				progress=(double)totalCount/81;
				
					if(this.stop) break;//as this part will spend a reasonable long period to be calculated, if the thread is asked to stop, stop continuing calculation
					
					px=rX;//this is for indicate generation progress
					py=rY;
					
					if(map[rX][rY]==0) continue;
					
					if(map[rX][rY]!=0)//if there still number left in the cell, we test it
					{
						Integer numberAt=map[rX][rY].intValue();// give the number to temp integer for possible later recover
						map[rX][rY]=0;//remove the value of the current cell in order to test it 
						CandidScanner.scanner(map, cand);//update the candid
						////
						
						
						boolean isSame=true;//this value indicate if the solutions are different or not
						for(int tt=0;tt<1;tt++)//this loop try to reduce the possibility of random solutions be same. however, we don't need it anymore
						{
							Integer[][] t1map =new Integer[9][9];//those arrays are temp array to compare the solutions
							Integer[][] t2map =new Integer[9][9];
							
							for(int i=0;i<9;i++)
							{
								for(int j=0;j<9;j++)
								{
									t1map[i][j]=map[i][j].intValue();//transfer the values of map into comparison arrays
									t2map[i][j]=map[i][j].intValue();
								}
							}
							
							LS.generatorLeft(t1map, 0, 0);//solve the comparison arrays by using two different solver
							LS.generatorRight(t2map, 0, 0);
							
							
							for(int i=0;i<9;i++)
							{
								for(int j=0;j<9;j++)
								{
									if(t1map[i][j]!=t2map[i][j])//compare if the comparison arrays are same, is they are not, there should be multi-solutions
										isSame=false;
								}
							}
							if(isSame ==false) break;
							
						}
						
						if(!isSame)//if there are multi-solutions recover the value of cells and moving on
						{
							map[rX][rY]=numberAt.intValue();
							CandidScanner.scanner(map, cand);
							//System.out.println("remove failed "+rX+" "+rY);
						}
						else//if there only is one solution keep the cell with removed status and moving on
							;//System.out.println("remove successful "+rX+" "+rY);
						
					}
				
			}
		}
	}
	
	
}
