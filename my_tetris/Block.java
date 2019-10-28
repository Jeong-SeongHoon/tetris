
package my_tetris;

import java.awt.Color;
import java.awt.Point;

public class Block {
    Point pt; //블럭의 좌표
    int b_size; //블럭의 크기
    Color color; //블럭의 색상
    boolean isVisible; //블럭을 화면에 표시하는 변수
    // 모양을 가진 블럭이 내려오다가 현재 블럭객체에
    // 자리잡으면 isVisible의 값이 true가 된다.
    
    boolean isfilled; //블럭이 쌓여있는지 여부

    public Block(Point pt, int b_size, Color color) {
        this.pt = pt;
        this.b_size = b_size;
        this.color = color;
    }
}
