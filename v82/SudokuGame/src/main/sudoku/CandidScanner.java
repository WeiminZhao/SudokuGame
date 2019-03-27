/**
 * 
 */
package main.sudoku;

/**
 * @author zwmsc
 *
 */
public class CandidScanner {//this class used for update the candid for the map
	
	public static void scanner(Integer[][] map,Numbers[][] candid)
	{
		
		for(int i=0;i<9;i++)
		{
			for(int j=0;j<9;j++)
			{
				candid[i][j]=new Numbers();//initial state of candidate records, candid for every cell default be 1 to 9 numbers. however, the candid only enable while the same cell in map is 0
			}
		}
		
		for(int i=0;i<9;i++)
		{
			for(int j=0;j<9;j++)
			{
				if(map[i][j]==0)
				{
					for(int c=0;c<9;c++)
					{
						candid[i][j].N.remove(map[i][c]);
					}
					for(int c=0;c<9;c++)
					{
						candid[i][j].N.remove(map[c][j]);
					}
					for(int cx=(i/3)*3;cx<(i/3)*3+3;cx++)
					{
						for(int cy=(j/3)*3;cy<(j/3)*3+3;cy++)
						{
							candid[i][j].N.remove(map[cx][cy]);
						}
					}
					
					
				}
				/*
				if(map[i][j]!=0)
				{
					candid[i][j].N.clear();
				}
				*/
			}
		}	
	}

}
