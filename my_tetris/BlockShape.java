package my_tetris;

//화면에 떨어지는 블럭 클래스
import java.awt.Color;
import java.awt.Point;
import java.util.Random;

public class BlockShape {

    public static final int RIGHT = 1;
    public static final int LEFT = -1;
    Point cur_pt = new Point(0, 0);// 블럭의 시작점
    int b_style; // 블럭모양

    int rotation;// 블럭의 각도(0,1,2,3 중 하나만)

    //실제 표현할 도형의값(4개) shape의 배열에서 1인 값
    Point[] b_shape = new Point[4];

    //블럭의 그림자를 표현할 좌표값 저장
    Point[] b_shadow = new Point[4];
    int b_shadow_dis;

    // 그림자 블럭의 색상 투명도 값
    //(0이면 나타나지 않는다.)
    int shadow_alpha = 100;

    Color color;// 블럭의 색상

    Random rnd = new Random();

    public BlockShape(int b_style, Color c) {
        this.b_style = b_style; //블럭의 모양 지정(0~6)
        this.color = c; //블럭의 색상 지정

        // this.rotation = (int)(Math.random()*4);
        this.rotation = rnd.nextInt(4);// 지정된 모양의 각도 지정

        // b_shape라는 배열에 그리고자 하는 
        //4개의 도형 위치값을 저장한다.
        int i = 0;
        for (int row = 0; row < Shape.SHAPE[b_style][rotation].length; row++) {
            for (int col = 0; col < Shape.SHAPE[b_style][rotation][row].length; col++) {
                //값이 1인 것들만 찾아낸다.
                if (Shape.SHAPE[b_style][rotation][row][col] == 1) {
                    this.b_shape[i++] = new Point(row, col);
                }
            }
        }
    }

    public void eraseShadowBlock(Block[][] b) {
        for (int i = 0; i < 4; i++) {
            b[b_shadow[i].x][b_shadow[i].y].isVisible = false;
        }
    }

//    public void getShadowBlock(Block[][] b) {
//        int value = 1;
//        bk:
//        while (value < 24) {
//            for (int i = 0; i < 4; i++) {
//                if (b[b_shape[i].x + value][b_shape[i].y].isfilled) {
//                    break bk;
//                }
//            }
//            value++;
//        }
//        b_shadow_dis = value - 1;
//    }
    public void getShadowBlock(Block[][] b) {

        boolean check = false;

        //일단 그림자 블럭을 현재블럭과 같게 만든다.
        for (int i = 0; i < 4; i++) {
            b_shadow[i] = new Point(b_shape[i].x, b_shape[i].y);
        }
        while (!check) {

            for (int i = 0; i < 4; i++) {
                b_shadow[i].x++; //그림자를 한칸 내린다.
            }

            //그림자 블럭 중 한곳이라도 아래쪽이 채워졌거나, 맨 아래줄에 아래줄이라면
            for (int i = 0; i < 4; i++) {
                if (b[b_shadow[i].x][b_shadow[i].y].isfilled == true || b_shadow[i].x == 24) {
                    check = true;
                    break;
                }
            }
            if (check) {
                break;
            }
        }

        // 그림자블럭을 한칸 올려야 제대로된 그림자가 나온다.
        for (int i = 0; i < 4; i++) {
            b_shadow[i].x--;
        }

        // 그림자블럭을 보여달라고 한다.
        for (int i = 0; i < 4; i++) {
            b[b_shadow[i].x][b_shadow[i].y].isVisible = true;
            b[b_shadow[i].x][b_shadow[i].y].color
                    = new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), shadow_alpha);
        }
    }

    // 블럭 좌우 이동(좌,우 키 눌렸을때)
    public void moveSide(Block[][] b, int move_to) {
        boolean check = false;

        for (int i = 0; i < 4; i++) {

            //좌측이나 우측에 하나라도 차있으면
            if (b[this.b_shape[i].x][this.b_shape[i].y + move_to].isfilled == true) {
                System.out.println("좌우이동금지(옆 블럭에걸림)");
                check = true; // 움직이면 안돼
                break;
            }

            //현재 좌측에 붙어 있는데 왼쪽으로 가라고 했거나
            //현재 우측에 붙어 있는데 오른쪽으로 가라고 했다면
            if ((this.b_shape[i].y == 4 && move_to == BlockShape.LEFT)
                    || this.b_shape[i].y == 13 && move_to == BlockShape.RIGHT) {
                System.out.println("좌우이동금지(벽에걸림)");
                check = true; // 움직이면 안돼
                break;
            }
        }

        //움직여도 된다면
        if (!check) {

            // 지금위치는 안보이게하고
            for (int i = 0; i < 4; i++) {
                b[this.b_shape[i].x][this.b_shape[i].y].isVisible = false;
                //b[this.b_shape[i].x+b_shadow_dis]
                //[this.b_shape[i].y].isVisible=false;
            }

            eraseShadowBlock(b);
            //좌측이나 우측으로 이동을 하고
            for (int i = 0; i < 4; i++) {
                this.b_shape[i].y += move_to;
            }
            //현재 기준점 변경
            this.cur_pt.y += move_to;

            // 블럭그림자를 그리고
            getShadowBlock(b);

            // 블럭을 보이게 하여라
            for (int i = 0; i < 4; i++) {
                b[this.b_shape[i].x][this.b_shape[i].y].isVisible = true;
                b[this.b_shape[i].x][this.b_shape[i].y].color = this.color;
            }

        }
    }

    // 블럭모양 변경(위쪽키 눌렸을때)
    public void changeBlockShape(Block[][] b) {
        boolean check = false;

        // 다음모양의 rotation값을 저장해놓는다.
        int new_rotation = this.rotation + 1;
        if (new_rotation > 3) {
            new_rotation = 0;
        }

        // 다음모양의 좌표값들을 저장해 놓는다.
        Point[] new_b_shape = new Point[4];

        int i = 0;
        for (int row = 0; row < Shape.SHAPE[this.b_style][new_rotation].length; row++) {
            for (int col = 0; col < Shape.SHAPE[this.b_style][new_rotation][row].length; col++) {

                //값이 1인 것들만 찾아낸다.
                if (Shape.SHAPE[this.b_style][new_rotation][row][col] == 1) {
                    new_b_shape[i++] = new Point(row + this.cur_pt.x, col + this.cur_pt.y);
                }
            }
        }

        // 만약 다음모양의 좌표값중 하나라도 채워져 있거나 현재모양의 x값이 23이라면
        for (i = 0; i < 4; i++) {
            if (b[new_b_shape[i].x][new_b_shape[i].y].isfilled == true || b_shape[i].x == 23) {
                System.out.println("모양변경금지");
                check = true; // 다음모양으로 변경 금지
                break;
            }
        }

        // 움직여도 되는 상태라면
        if (!check) {
            boolean check_y = false;

            // 다음 모양 블럭중 하나라도 y경계선 밖이거나 x선경계 밖일경우
            for (i = 0; i < 4; i++) {
                if (new_b_shape[i].y < 4 || new_b_shape[i].y > 13 || new_b_shape[i].x > 23) {
                    check_y = true;
                }
            }

            // y경계선 밖으로 나가 있을경우
            // 나간만큼 안쪽으로 다음블럭모양을 이동 시키고
            // 기준점도 이동시켜주어야 한다.
            // 또한 맨마지막줄에서 다음블럭모양으로 바꿀때에도 x경계선 밖으로 나간다면 블럭모양을 이동시키고
            // 기준점도 이동시켜주어야 한다.
            if (check_y) {
                int y_min = Tetris.B_COL - 1, y_max = 0; // 새로운 좌표의 y값의 최좌측값, 최 우측값
                int x_max = 0; // 새로운 좌표의 최하단값

                //새로운 좌표의 최좌측값
                for (i = 0; i < 4; i++) {
                    if (y_min > new_b_shape[i].y) {
                        y_min = new_b_shape[i].y;
                    }
                }
                //새로운 좌표의 최우측값
                for (i = 0; i < 4; i++) {
                    if (y_max < new_b_shape[i].y) {
                        y_max = new_b_shape[i].y;
                    }
                }

                //새로운 좌표의 최하단
                for (i = 0; i < 4; i++) {
                    if (x_max < new_b_shape[i].x) {
                        x_max = new_b_shape[i].x;
                    }
                }

                //다음 모양이 왼쪽으로 나가 있다면
                if (y_min < 4) {
                    int gap = 4 - y_min; // 밖으로 나간 차이
                    for (i = 0; i < 4; i++) {
                        new_b_shape[i].y += gap; // 밖으로 나간 차이만큼 각각 다음모양블럭에 더해준다
                    }
                    this.cur_pt.y += gap; // 기준점 변경 
                }

                //다음 모양이 오른쪽으로 나가 있다면
                if (y_max > 13) {
                    int gap = y_max - 13; // 밖으로 나간 차이
                    for (i = 0; i < 4; i++) {
                        new_b_shape[i].y -= gap; // 밖으로 나간 차이만큼 각각 다음모양블럭에 빼준다
                    }
                    this.cur_pt.y -= gap; // 기준점 변경

                }

                //다음 모양이 아래쪽으로 나가 있다면
                if (x_max > 23) {
                    int gap = x_max - 23;
                    for (i = 0; i < 4; i++) {
                        new_b_shape[i].x -= gap; // 밖으로 나간 차이만큼 각각 다음모양블럭에 더해준다
                    }
                    this.cur_pt.x -= gap; // 기준점 변경 
                }
            }

            // 지금 4군데는 안보여지게하고
            for (i = 0; i < 4; i++) {
                b[this.b_shape[i].x][this.b_shape[i].y].isVisible = false;
                //   b[this.b_shape[i].x+b_shadow_dis]
                // [this.b_shape[i].y].isVisible=false;
            }

            //블럭그림자를 안보이게 하고
            eraseShadowBlock(b);

            // 바뀔 모양의 좌표값을 넣고
            for (i = 0; i < 4; i++) {
                this.b_shape[i] = new_b_shape[i];
            }
            // 블럭그림자를 그리고
            getShadowBlock(b);

            // 그 위치에 보이게 해라
            for (i = 0; i < 4; i++) {
                b[this.b_shape[i].x][this.b_shape[i].y].isVisible = true;
                b[this.b_shape[i].x][this.b_shape[i].y].color = this.color;
            }

            this.rotation = new_rotation;
        }
    }

    // 블럭 내리 꽂기(스페이스바 눌렸을때)
    public void goDown(Block[][] b) {
        //현재 블럭을 보이지 않게 하고 그림자 블럭의 위치를 얻어온다.
        for (int i = 0; i < 4; i++) {
            b[this.b_shape[i].x][this.b_shape[i].y].isVisible = false;
        }

        getShadowBlock(b);

        //현재블럭을 그림자 블럭으로 바꿔버린다.
        this.b_shape = this.b_shadow;
//        for(int i=0; i<4; i++){
//            this.b_shape[i].x = this.b_shadow[i].x;
//            this.b_shape[i].y = this.b_shadow[i].y;
//        }

        //그 후에 그림자 위치에 보여주면서 채워버린다.
        for (int i = 0; i < 4; i++) {
            b[this.b_shape[i].x][this.b_shape[i].y].isVisible = true;
            b[this.b_shape[i].x][this.b_shape[i].y].isfilled = true;
            b[this.b_shape[i].x][this.b_shape[i].y].color = this.color;
        }

    }

    // 시간에 따라 블럭이 내려가는 기능
    // (바닥과 다른 블럭과의 충돌체크)
    public void moveDown(Block[][] b) {
        boolean check = false;
        // 아래쪽이 차있거나, 마지막 줄이라면
        for (int i = 0; i < 4; i++) {
            if (b[this.b_shape[i].x + 1][this.b_shape[i].y].isfilled == true || this.b_shape[i].x == 23) {
                System.out.println("더 이상 내려갈 수 없습니다.");
                check = true; // 움직이면 안돼
                break;
            }
        }

        // 움직여도 되는 상태라면
        if (!check) {

            // 지금위치는 안보이게하고
            for (int i = 0; i < 4; i++) {
                b[this.b_shape[i].x][this.b_shape[i].y].isVisible = false;
                //그림자 처리
                //b[this.b_shape[i].x+b_shadow_dis][this.b_shape[i].y].isVisible=false;
            }
            // 그림자를 지운다음
            eraseShadowBlock(b);
            // 아래로 한칸 이동해라
            for (int i = 0; i < 4; i++) {
                this.b_shape[i].x += 1;
            }
            //그림자 위치를 얻어오고
            getShadowBlock(b);

            //블럭을 보여줘라
            for (int i = 0; i < 4; i++) {
                b[this.b_shape[i].x][this.b_shape[i].y].isVisible = true;
                b[this.b_shape[i].x][this.b_shape[i].y].color = this.color;
            }

//            //그림자를 보여줘라
//            for(int i=0; i<4; i++){
//                b[b_shape[i].x+b_shadow_dis]
//                 [b_shape[i].y].isVisible = true;
//               b[b_shape[i].x+b_shadow_dis]
//                 [b_shape[i].y].color = new Color(
//                        color.getRed(), color.getGreen(),
//                        color.getBlue(), shadow_alpha);
//            }
            //현재 기준점 증가
            this.cur_pt.x += 1;
        } // 움직이면 안돼는 상태라면
        else {
            // 채워넣어라
            for (int i = 0; i < 4; i++) {
                b[this.b_shape[i].x][this.b_shape[i].y].isfilled = true;
                b[this.b_shape[i].x][this.b_shape[i].y].color = this.color;
            }

        }
    }

    public void rotation(Block[][] b) {
        // 위치값(rotation)을 1증가 시킨다.
        rotation = ++rotation % 4;

        //턴이 된 블럭모양을 담을 배열
        // b_shape배열과 같은 배열이 필요함!
        Point[] p = new Point[4];

        int idx = 0;// 위의 p배열을 접근하는 인덱스 값

        // 선택된 모양은 이미 현재 객체가 생성될 때
        // 받아서 b_style에 저장되어 있다. 하지만 각도를
        // 변경하기 위해서 rotation의 값을 증가시킨 것이다.
        // 그러므로 턴이된 모양의 위치 값 4개를 가져온다.
        for (int row = 0; row < Shape.SHAPE[b_style][rotation].length; row++) {
            for (int col = 0; col < Shape.SHAPE[b_style][rotation][row].length; col++) {
                //값이 1인 것들만 찾아낸다.
                if (Shape.SHAPE[b_style][rotation][row][col] == 1) {
                    p[idx++] = new Point(row, col);
                }
            }
        }

        boolean check = false;
        int leftWall = 0;
        int rightWall = 0;
        for (int i = 0; i < 4; i++) {
            if (b[p[i].x + cur_pt.x][p[i].y + cur_pt.y].isfilled) {// true일경우
                check = true;
                if (p[i].y + cur_pt.y >= 4 && p[i].y + cur_pt.y <= 13) {
                    //break;
                }
                if (p[i].y + cur_pt.y == 14 || p[i].y + cur_pt.y == 3) {
                    leftWall++;
                    //break;
                }
                if (p[i].y + cur_pt.y == 15 || p[i].y + cur_pt.y == 2) {
                    rightWall++;
                    //break;
                }
            }
        }

        if (check) {
            for (int i = 0; i < 4; i++) {
                //현재 블럭 지우기
                b[b_shape[i].x][b_shape[i].y].isVisible = false;

                // 현재 그림자 지우기
                b[b_shape[i].x + b_shadow_dis][b_shape[i].y].isVisible = false;

                b_shape[i].x = p[i].x + cur_pt.x;
                b_shape[i].y = p[i].y + cur_pt.y + leftWall - rightWall;
            }
            //배열 p의 값들을 b_shape배열에 저장한다.
            // b_shape = p; // 이렇게 단순히 변경하면
            // process메서드의 if문에서 b_shape[0].x 등의 비교문에서
            // 걸러져 항상 새로운 블럭이 생기게된다.

            getShadowBlock(b);//그림자 거리 측정

            for (int i = 0; i < 4; i++) {
                // 턴이 된 블럭과 그림자를 그린다.
                b[b_shape[i].x + b_shadow_dis][b_shape[i].y].isVisible = true;
                b[b_shape[i].x + b_shadow_dis][b_shape[i].y].color
                        = new Color(color.getRed(), color.getGreen(),
                                color.getBlue(), shadow_alpha);

                //블럭모양
                b[b_shape[i].x][b_shape[i].y].isVisible = true;
                b[b_shape[i].x][b_shape[i].y].color = color;
            }
        }
    }
}
