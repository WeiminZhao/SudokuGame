/**
 * 
 */
package main.sudoku;

import java.util.ArrayList;

/**
 * @author zwmsc
 *
 */
public class MainSolvGen {//this class used to generate a complete map for puzzlegenv2 to generate a puzzle

	/**
	 * @param args
	 */
	boolean stop;//this variable used to force calculation to stop
	
	public MainSolvGen() {
		stop=false;
	}

	public boolean generator(Integer[][] map, int currentX, int currentY) {
		////////////////////
		//System.out.print("\r");
		//System.out.print(currentX+"||"+currentY);
		
		
		////////////////////////////
		if(stop) return true;//if request stop, stop the calculation
		
		if (currentX > 8) {//return when generate complete map successfully
			return true;
		}
		
		if(map[currentX][currentY]!=0)//this part is used for solver (if this spot has a number in it, just jump to next one)
		{
			if (currentY < 8) {//moving on
				return generator(map, currentX, currentY+1);//the success or failure of built map will return to this point from last round
			} else {
				return generator(map, currentX+1, 0);//the success or failure of built map will return to this point from last round
			}
		}
		
		boolean ifAcceptable = false;//to check if the return is from the successful complete map, otherwise the return will be caused by fail insert
		ArrayList<Integer> numberUsed= new ArrayList<Integer>();//to record the number already used in this spot
		
		while (!ifAcceptable) {//if the map is not successfully generated (last round failed), either will continue trying in this round or return the failure to the previous round level(when run out of number)
			map[currentX][currentY]=0;//reset every time while re-do this spot, to avoid if this round do not have the acceptable number the last tried number left there affect the other generation;
			
			ArrayList<Integer> currentLeft = new ArrayList<Integer>();

			for (int i = 1; i <= 9; i++) {
				currentLeft.add(i);//the default is every number is suitable
			}
			
			for (int i = 0; i < numberUsed.size(); i++)//remove the number already used in this spot this round
				currentLeft.remove(numberUsed.get(i));

			for (int i = 0; i < 9; i++)//remove the number in same line
				currentLeft.remove(map[i][currentY]);
			for (int i = 0; i < 9; i++)//remove the number in same line
				currentLeft.remove(map[currentX][i]);

			int currentBlockStartX = (currentX / 3)*3;
			int currentBlockStartY = (currentY / 3)*3;
			// 0 1 2 3 4 5 6 7 8 
			//remove the number in same block
			for (int i = currentBlockStartX; i < currentBlockStartX + 3; i++) {
				for (int j = currentBlockStartY; j < currentBlockStartY + 3; j++) {
					currentLeft.remove(map[i][j]);
				}
			}

			if (currentLeft.size() != 0) {
				map[currentX][currentY] = currentLeft.get((int) (Math.random() * currentLeft.size()));//if there are suitable numbers random select one to fill in this spot
				numberUsed.add(map[currentX][currentY]);//record the used number in this round
				if (currentY < 8) {//moving on
					ifAcceptable=generator(map, currentX, currentY+1);//the success or failure of built map will return to this point from last round
				} else {
					ifAcceptable=generator(map, currentX+1, 0);//the success or failure of built map will return to this point from last round
				}

				
			} else if (currentLeft.size() == 0) {//if no number suitable return the failure of insert and return to the previous round level
				//map[currentX][currentY]=0;//reset every time while re-do this spot;
				return false;
			}
		}
		return ifAcceptable;

	}
}
