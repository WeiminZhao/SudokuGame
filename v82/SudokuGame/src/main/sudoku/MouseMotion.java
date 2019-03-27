package main.sudoku;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class MouseMotion implements MouseMotionListener{

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		int mdx = e.getX();
		int mdy = e.getY();
		Game.mousehx=mdx;
		Game.mousehy=mdy;
		Game.mousex=mdx;
		Game.mousey=mdy;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
		int mhx = e.getX();
		int mhy = e.getY();
		Game.mousehx=mhx;
		Game.mousehy=mhy;
		//System.out.println("hover: "+mhx+" "+mhy);
		
	}

}
