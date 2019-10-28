package my_tetris;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;

public class Tetris extends JFrame {

    JPanel pan; // 메인화면
    JPanel pan2;// 게임 종료시 작은화면
    JDialog d; //게임종료 대화상자

    //테트리스의 필요한 블럭배열
    static final int W = 600;// 창 너비
    static final int H = 630;//창 높이
    static final int B_ROW = 28;// 테트리스 블럭배열의 행의 수
    static final int B_COL = 18;// 테트리스 블럭배열의 열의 수

    Block[][] block = new Block[B_ROW][B_COL];// 테트리스 블럭판

    Block[][] block_next = new Block[4][4]; //미리보기 블럭판

    int score; //게임스코어
    int score_next = 500; // 다음 스테이지로 넘어가기위한 스코어

    Font font = new Font(Font.DIALOG, Font.BOLD, 22);
    int b_size = 30;// 블럭 사이즈
    int bn_size = 60;// 미리보기판의 블럭 사이즈
    int bf_cnt; //지워질 라인의 개수 저장(최대 4)

    BlockShape block_s;//블럭모양
    BlockShape block_sn;//다음 블럭
    int selected;//상태값
    //색상 7가지
    Color[] color = {new Color(255, 0, 0),
        new Color(0, 255, 0),
        new Color(0, 0, 255),
        new Color(255, 255, 25),
        new Color(100, 150, 0),
        new Color(20, 255, 255),
        new Color(100, 125, 200)};

    //javax.swing의 타이머 활용
    int time_speed = 800;
    Timer timer = new Timer(time_speed, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            process();
        }
    });
    //고 득점에 따라 레벨등급을 지정할 때는
    // 지연시간을 위와 같이 500이라고 주지 않고
    // 변수를 활용해야 한다.
    
    JLabel l_now, l_next;
    JButton bt_start, bt_keyMouse;
    JScrollPane sp_msg;
    JTextArea ta_msg;
    int stage_num = 1; // 시작스테이지는 1단계
    boolean isKeyboard = true; //키보드모드일 때 true,
    //마우스모드일 때 false
    boolean isPause;//게임중단여부
    boolean isGameStart;//게임 시작여부
    boolean isGameEnd; //게임 종료여부

    //이미지 준비
    Image back1 = new ImageIcon(
            "src/my_tetris/설현.gif").getImage();
    Image game_over = new ImageIcon("src/my_tetris/게임오버.PNG").getImage();
    Image su_img = new ImageIcon("src/my_tetris/su.png").getImage();
    Image stage_img = new ImageIcon("src/my_tetris/stage.png").getImage();

    // 랜덤객체
    Random rnd = new Random();

    int rnd_num;// 난수(블럭 모양을 선택) 
    int rnd_num2; // 난수(미리보기 판에 보여질 블럭모양)

    ActionListener al = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            //우선 이벤트 발생 객체 구별
            Object obj = e.getSource();
            if (obj == bt_start) {
            	//시작할때
                if (!isGameStart) {//isGameStart가 false일때
                    gameStart();
                    bt_start.setText("  Pause  ");
                }
                
                //게임중일때
                else{
                	if(!isPause){
                		ta_msg.append("게임 중지\r\n");
                		timer.stop();
                		isPause = true;
                		bt_start.setText("  Resume  ");
                        ta_msg.append("키보드 감지 중단\r\n");
                        Tetris.this.removeKeyListener(key);
                	}
                	else{
                		ta_msg.append("게임 재개\r\n");
                		timer.restart();
                		isPause = false;
                		bt_start.setText("  Pause  ");
                        ta_msg.append("키보드 감지 작동\r\n");
                        Tetris.this.addKeyListener(key);
                	}
                }
            }
        }
    };

    KeyAdapter key = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            int key_code = e.getKeyCode();
            switch (key_code) {
                case KeyEvent.VK_RIGHT:
                    block_s.moveSide(block, BlockShape.RIGHT);
                    break;
                case KeyEvent.VK_LEFT:
                    block_s.moveSide(block, BlockShape.LEFT);
                    break;
                case KeyEvent.VK_UP:
                    block_s.changeBlockShape(block);
                    break;
                case KeyEvent.VK_DOWN:
                    block_s.moveDown(block);
                    break;
                case KeyEvent.VK_SPACE:
                    block_s.goDown(block);
                    break;
            }
            pan.repaint();
            // 채워졌다면 블럭을 새로 만들어야 함.
            for (int i = 0; i < 4; i++) {
                if (block[block_s.b_shape[i].x][block_s.b_shape[i].y].isfilled == true) {
                    addBlock();
                }
            }
        }

    };

    public Tetris() {

        initBlock(); // 블럭생성 및 초기화
        initPan(); // 

        //버튼 작업
        bt_start = new JButton("  Start!  ");
        bt_start.setBounds(330, 550, 250, 30);
        pan.add(bt_start);//패널의 레이아웃이 null로 되어야 한다.

        bt_start.addActionListener(al); // 버튼에 마우스 감지
        
        //점수 Label
        l_now = new JLabel("현재점수:");
        l_next = new JLabel("목표점수:");	
        l_now.setBounds(330, 330, 80, 20);
        l_next.setBounds(330, 370,80,20);
        pan.add(l_now);
        pan.add(l_next);
        
        //게임상황판
        sp_msg = new JScrollPane();
        ta_msg = new JTextArea();
        sp_msg.setViewportView(ta_msg);
        sp_msg.setBounds(330, 410, 250, 130);
        pan.add(sp_msg);
        
        setLocation(100, 50);
        setResizable(false);//창 크기저정 불가
        pack();
        setVisible(true);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Frame에 버튼등이 존재할 경우 포커스가
        // 버튼에 가 있다. 포커스를 다시 Frame에 가져다 놓는다.
        this.requestFocus();
    }

    //블럭(배열)초기화 기능
    public void initBlock() {

        //큰 뒷판의 블럭(배열) 생성 및 초기화
        for (int row = 0; row < block.length; row++) { //행 반복
            for (int col = 0; col < block[row].length; col++) {//열 반복
                //각 블럭(상자 == 격자)를 생성하여
                //배열에 저장한다.
                block[row][col] = new Block(
                        new Point(col * b_size - 120, row * b_size - 120),
                        b_size, Color.yellow);
            }
        }

        //미리보기 판의 블럭(배열) 초기화
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                block_next[row][col] = new Block(
                        new Point(col * bn_size + 330, row * bn_size + 30),
                        bn_size, Color.red);
            }
        }
    }

    //게임화면 초기화
    public void initPan() {
        //화면 패널 생성
        pan = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.clearRect(0, 0, W, H);//화면 전체 청소

                //배경 설정
                g2.drawImage(back1, 0, 0, 300, 600,
                        0, 0, back1.getWidth(this), back1.getHeight(this), this);

                // 블럭판 그리기
                for (int row = 0; row < block.length; row++) {
                    for (int col = 0; col < block[row].length; col++) {
                        //배열로부터 하나의 블럭객체를 가져온다.
                        Block b = block[row][col];

                        //보여지는 블럭을 그리는 부분
                        if (b.isVisible) {
                            g2.setColor(b.color);
                            g2.fillRect(b.pt.x, b.pt.y, b.b_size, b.b_size);
                        }
                        //보여지지 않는 블럭은 테두리만 그림
                        if (col > 3 && row < 24 && col < 14 && row > 3) {
                            g2.setColor(Color.black);
                            g2.drawRect(b.pt.x, b.pt.y, b.b_size, b.b_size);
                        }
                    }
                }

                //미리보기 판(다음 블럭)
                for (int row = 0; row < 4; row++) {
                    for (int col = 0; col < 4; col++) {
                        Block b = block_next[row][col];
                        if (b.isVisible) {
                            g2.setColor(b.color);
                            g2.fillRect(b.pt.x, b.pt.y, b.b_size, b.b_size);
                        }
                        g2.setColor(Color.black);
                        g2.drawRect(b.pt.x, b.pt.y, b.b_size, b.b_size);
                    }
                }
                
                //현재스코어 그리기
                int imsi =score;
                int imsi2 = 0;
                for(int i=0; i<6; i++){
                    imsi2 = (int)(imsi/Math.pow(10, 5-i));
                    imsi = (int)(imsi%Math.pow(10, 5-i));
                    
                    g2.drawImage(su_img, 390+(32*i),325,390+(32*(i+1)),355,
                            imsi2%10*50,0,imsi2%10*50+50,90, this);
                }
                // 목표스코어 그리기
                imsi =score_next;
                imsi2 = 0;
                for(int i=0; i<6; i++){
                    imsi2 = (int)(imsi/Math.pow(10, 5-i));
                    imsi = (int)(imsi%Math.pow(10, 5-i));
                    
                    g2.drawImage(su_img, 390+(32*i),365,390+(32*(i+1)),395,
                            imsi2%10*50,0,imsi2%10*50+50,90, this);
                }
                
                // 스테이지 값
                int s = stage_num-1;
                g2.drawImage(stage_img, 350,280,550,310,0,s*90,490,s*90+90,this);
                
               
            }

        };
        this.add(pan);
        this.setPreferredSize(new Dimension(W, H));
        //this.setPreferredSize(new Dimension(1000, 1000));
    }

    //타이머에 의해 호출되는 메서드
    private void process() {

        
        // 블럭이동
        block_s.moveDown(block);
        //채워졌다면 블럭다시 생성
        for (int i = 0; i < 4; i++) {
            if (block[block_s.b_shape[i].x][block_s.b_shape[i].y].isfilled == true) {
                addBlock();
            }
        }

        // 블럭라인체크
        lineCheck(); // 점수올리기 기능까지
        
        // 게임레벨체크
        stageCheck();
        checkGameEnd();
        // 게임화면 다시그리기
        pan.repaint();
    }
    public void stageCheck(){
    	// 현재 스테이지가 1,2,3,4,5,6단계중 한단계라면
        if (stage_num < 7) {
        	// 목표점수를 넘어 섰다면
            if (score > score_next) {
                stage_num++; // 다음 스테이지로 이동

                ta_msg.append(stage_num + " 스테이지 진입\r\n");
                // 목표점수 변경
                score_next += 1000;
                ta_msg.append("다음 스테이지 목표 점수 :"+score_next+"\r\n");
                // 게임속도 변경
                time_speed -= 100;
                timer.setDelay(time_speed);
            }
        }
    }
    public void lineCheck(){
        boolean check = false;
        int[] r_num = new int[4];//삭제할 행 번호들
        bf_cnt = 0;
        
        //화면 아래쪽부터 행 하나씩(23 -> 4) 검사하는 반복문
        //for(int i=23; i>3; i--){ //아래부터 지우게 되면 한블럭이 타이머 뒤에 지워지게 된다.
        for(int i=4; i<=23; i++){
            check = true;
            for(int j=4; j<14; j++){
                if(!block[i][j].isfilled){ //채워지지 않은 칸을 만나면
                    check = false;
                    break; //가장 가까운 반복문 탈출
                }
            }
            //현재 지점에 왔을 때 check는 계속 true를
            // 유지할 수도 있지만, 중간에 if문에 의해서
            // false로 변경되었을 수도 있다.
            if(check)
                r_num[bf_cnt++] = i;
            
            if(bf_cnt > 3)
                break;
        }
        
        
        //삭제할 행들은 삭제한다. 이때
        // 삭제할 행의 바로 위에 있는 행을 내려야 한다.
        int r=0;
        while(r < bf_cnt){
            
            for(int i=r_num[r]; i > 3; i--){
                // 위에서 행이 지정되었으므로 열을 반복하는 
                // 반복문
                for(int j=4 ; j<14 ; j++){
                    //삭제할 칸의 바로 위에 있는 사각형이
                    // 채워졌다면
                    if(block[i-1][j].isfilled){
                        block[i][j].isfilled = block[i-1][j].isfilled;
                        block[i][j].isVisible = block[i-1][j].isVisible;
                        block[i][j].color = block[i-1][j].color;
                    }else{
                        block[i][j].isfilled = false;
                        block[i][j].isVisible = false;
                    }
                }
            }
            r++;
        }
        switch (bf_cnt) {
            case 1:
            	ta_msg.append("1칸 없애기성공 +100점\r\n");
                score += 100;
                break;
            case 2:
            	ta_msg.append("2칸 없애기성공 +300점\r\n");
                score += 300;
                break;
            case 3:
            	ta_msg.append("3칸 없애기성공 +600점\r\n");
                score += 600;
                break;
            case 4:
            	ta_msg.append("4칸 없애칸성공 +1000점\r\n");
                score += 1000;
                break;
        }
    }

//    private void checkLine() {
//
//        //지워져야 하는 행의 수를 저장할 리스트
//        ArrayList<Integer> eraseRow = new ArrayList<Integer>();
//
//        // 눈에 보이는 부분만 실제 게임판이므로
//        // 그 부분만 검사를 한다.
//        for (int row = 4; row <= 23; row++) {
//            boolean check = true;
//            for (int col = 4; col <= 13; col++) {
//                //열에 하나라도 빈칸이 있으면 지워질수 없다.
//                if (block[row][col].isfilled == false) {
//                    check = false;
//                    break;
//                }
//            }
//            //해당 열이 지워져야 한다면 리스트에 추가
//            if (check) {
//                eraseRow.add(row);
//            }
//        }
//        //지워져야 할 행이 있으면
//        if (eraseRow.size() != 0) {
//            for (int i = 0; i < eraseRow.size(); i++) {
//                //해당 행을 지우고
//                eraseLine(block, eraseRow.get(i));
//                
//                System.out.println(eraseRow.get(i)+"행 삭제");
//                //점수를 올린다.
//                score++;
//            }
//        }
//        //다음번 라인체크를 위해 리스트를 비운다.
//        eraseRow.removeAll(eraseRow);
//        //지운대로 다시 그린다.
//        pan.repaint();
//        l_score.setText(Integer.toString(score));
//    }
//
//    private void eraseLine(Block[][] block, int row) {
//
//        // 그라인을 없애고
//        for (int col = 4; col <= 13; col++) {
//            block[row][col].isfilled = false;
//            block[row][col].isVisible = false;
//        }
//
//        //해당라인 위쪽의 블럭을 이동 시킨다. 
//        for (int i = row - 1; i > 3; i--) {
//            for (int col = 4; col <= 13; col++) {
//                block[i + 1][col].color = block[i][col].color;
//                block[i + 1][col].isfilled = block[i][col].isfilled;
//                block[i + 1][col].isVisible = block[i][col].isVisible;
//
//                //내려온 공간에는 빈공간으로 냅뚸야 하므로
//                block[i][col].isfilled = false;
//                block[i][col].isVisible = false;
//            }
//        }
//    }

    //다음블럭 생성 한 후,
    //블럭(도형)을 생성하여 계속 이동시키는 기능
    private void addBlock() {

        // 처음 막 시간한 상태라면 현재 블럭이 비어 있다
        // 따라서 block_s가 null을 가르키게 된다.
        if (block_s == null) {
            rnd_num = rnd.nextInt(7);
            block_sn = new BlockShape(rnd_num, color[rnd_num]);
        } else {
            //이미 블럭이 선택되어서
            // 떨어지고 있는 상태이다.
            // 미리보기 판 지우기
            for (int i = 0; i < 4; i++) {
                block_next[block_sn.b_shape[i].x][block_sn.b_shape[i].y].isVisible = false;
            }
        }
        block_s = block_sn;//다음 블럭을 현재 블럭으로 지정!
        
        //블럭의 기준점을 pan의 가운데로...
        block_s.cur_pt.x += 1;
        block_s.cur_pt.y += 7;

        //실제 블럭의 모양을 pan에 그리기 전에
        //모양도(4X4)에 정의한다.
        for (int i = 0; i < 4; i++) {
            block_s.b_shape[i].x += 1;
            block_s.b_shape[i].y += 7;

            //pan에 그리기
            block[block_s.b_shape[i].x][block_s.b_shape[i].y].isVisible = true;
            block[block_s.b_shape[i].x][block_s.b_shape[i].y].color = block_s.color;
        }

        // 블럭을 생성한 후에 기준점을 변경하고 블럭을 그린다음에 
        // 블럭그림자를 보여주어야 한다.
        // 그렇지 않으면 생성 후 timer시간동안 만큼은 블럭그림자 객체가 없는 상태가 된다
        // (가만히 내비두면 timer시간 후에 한칸아래로 이동하면서 블럭객체를 다시 만드므로)
        // 그렇게 되면
        // timer시간 전에 블럭객체에 접근하는 모든 활동(모양변경, 이동, 내리꽂기 등)이 에러가 난다
        // 널포인터에 접근하므로
        block_s.getShadowBlock(block);

        //다음 블럭 선별(같은 블럭을 연속해서 나타남을
        // 막는 반복문)
        for (rnd_num2 = rnd.nextInt(7); rnd_num == rnd_num2;) {
            rnd_num2 = rnd.nextInt(7);
        }

        //다음 블럭 생성
        rnd_num = rnd_num2;
        block_sn = new BlockShape(rnd_num, color[rnd_num]);

        //미리보기 판에 다음블럭을 그린다.
        for (int i = 0; i < 4; i++) {
            block_next[block_sn.b_shape[i].x][block_sn.b_shape[i].y].isVisible = true;
            block_next[block_sn.b_shape[i].x][block_sn.b_shape[i].y].color = block_sn.color;
        }
    }

    //게임 시작(초기화) 기능
    private void gameStart() {
        if (!isGameStart) {
        	ta_msg.append("게임 시작\r\n");
            // 게임시작후 키보드 동작 감지
            ta_msg.append("키보드 감지 작동\r\n");
            this.addKeyListener(key);
            //시작버튼을 누르면 바로 블럭한개 생성

            addBlock();
            isGameStart = true;
            isGameEnd = false;
        }
        score = 0;
        stage_num = 1;
        timer.start();
        this.requestFocus();
    }

    //게임 종료 기능 - 화면처리
    //보여지는 블럭배열의 가장 윗행을 체크하는 기능
    private void checkGameEnd() {
        boolean check = false;
        for (int col = 4; col <= 13; col++) {
            if (block[3][col].isfilled== true) {
                check = true;
                break;
            }
        }
        
        if(check){
            gameOver(); // 게임종료 화면처리와 초기화 작업
        }
    }
    
    private void gameOver(){
        isGameStart = false;
        isGameEnd = true;
        timer.stop();
        d = new JDialog(this,"게임종료!!!");
        //대화상자에 표현할 패널 생성
        pan2 = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                g.clearRect(0, 0, 300, 280);
                g.drawImage(game_over, 0,0, d.getWidth(), d.getHeight(), 0, 0, game_over.getWidth(this), game_over.getHeight(this), this);
                //이미지 그리기
            }
        };
        pan2.setPreferredSize(new Dimension(550,280));
        d.add(pan2);
        
        //대화상자의 위치지정
        d.setLocation(this.getLocation().x+50, this.getLocation().y+100);
        d.pack();
        d.setResizable(false);
        d.setVisible(true);
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    public static void main(String[] args) {
        new Tetris();
    }
}
