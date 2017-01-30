package com.example.matt.retrogame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    Canvas canvas;
    SquashCourtView squashCourtView;

    //Sound

    //display Control
    Display display;
    Point size;
    int screenWidth;
    int screenHeight;

    //Game objects
    int racketWidth;
    int racketHeight;
    Point racketPosition;

    Point ballPosition;
    int ballWidth;

    //ball movement
    boolean ballIsMovingLeft;
    boolean ballIsMovingRight;
    boolean ballIsMovingUp;
    boolean ballIsMovingDown;
    int ballDirection;

    //racket move
    boolean racketIsMovingLeft;
    boolean racketIsMovingRight;

    //stats
    long lastFrameTime;
    int fps;
    int score;
    int lives;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        squashCourtView = new SquashCourtView(this);
        setContentView(squashCourtView);


        //Sound bla bla

        //screen
        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);


        screenWidth = size.x ;
        screenHeight = size.y - 20;

        //the game objects
        racketPosition = new Point();
        racketPosition.x = screenWidth /2;
        racketPosition.y = screenHeight - 20;
        racketWidth = screenWidth / 8;
        racketHeight = 8;

        ballWidth = screenWidth / 35;
        ballPosition = new Point();
        ballPosition.x = screenWidth / 2;
        ballPosition.y = 1 + ballWidth;

        lives = 3;

    }

    @Override
    protected void onStop() {
        super.onStop();

        while (true){
            squashCourtView.pause();
            break;
        }

        finish();

    }

    @Override
    protected void onPause() {
             super.onPause();
        squashCourtView.pause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        squashCourtView.resume();

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_BACK){
            squashCourtView.pause();
            finish();
            return true;
        }
        return false;
    }

    public class SquashCourtView extends SurfaceView implements Runnable {

        Thread ourThread = null;
        SurfaceHolder ourHolder;
        volatile boolean playingSquash;
        Paint paint;

        public SquashCourtView(Context context) {
            super(context);
            ourHolder = getHolder();
            paint = new Paint();
            ballIsMovingDown = true;

            //send ball in random direction
            Random randomNumber = new Random();
            int ballDirection = randomNumber.nextInt(3);

            switch (ballDirection) {
                case 0:
                    ballIsMovingLeft = true;
                    ballIsMovingRight = false;
                    break;
                case 1:
                    ballIsMovingRight = true;
                    ballIsMovingLeft = false;
                    break;
                case 2:
                    ballIsMovingRight = false;
                    ballIsMovingLeft = false;
                    break;
            }
        }

        @Override
        public void run() {
            while (playingSquash) {
                updateCourt();
                drawCourt();
                controlFPS();
            }
        }


        public void updateCourt() {

            if (racketIsMovingRight) {
                if(racketPosition.x >= screenWidth){
                    racketPosition.x = screenWidth - 10;
                }else {
                    racketPosition.x = racketPosition.x + 10;
                }
            }
            if (racketIsMovingLeft) {
                if(racketPosition.x <= 0){
                    racketPosition.x = 0;
                }else {
                    racketPosition.x = racketPosition.x - 10;
                }
            }

            if(racketPosition.x >= screenWidth){
                racketPosition.x = screenWidth;
            }


            //detect collisions

            //hit right of screen
            if (ballPosition.x + ballWidth > screenWidth) {
                ballIsMovingLeft = true;
                ballIsMovingRight = false;
                //sound here
            }
            if (ballPosition.x < 0) {
                ballIsMovingLeft = false;
                ballIsMovingRight = true;
                //SoundHere
            }


            //ball has hit bottom
            if (ballPosition.y > screenHeight - ballWidth) {
                lives = lives - 1;
                if (lives == 0) {
                    lives = 3;
                    score = 0;
                    //sound pool stuf here
                }

                ballPosition.y = 1 + ballWidth;//back to the top

                Random randomNumber = new Random();
                int startX = randomNumber.nextInt(screenWidth - ballWidth) + 1;

                ballPosition.x = startX + ballWidth;

                int ballDirection = randomNumber.nextInt(3);

                switch (ballDirection) {
                    case 0:
                        ballIsMovingLeft = true;
                        ballIsMovingRight = false;
                        break;
                    case 1:
                        ballIsMovingLeft = false;
                        ballIsMovingRight = true;
                        break;
                    case 2:
                        ballIsMovingLeft = false;
                        ballIsMovingRight = false;
                        break;
                }
            }

            //we hit the top of the screen

            if (ballPosition.y <= 0) {
                ballIsMovingUp = false;
                ballIsMovingDown = true;
                ballPosition.y = 1;
                //Sound pool stuf
            }

            if (ballIsMovingDown) {
                ballPosition.y += 2; //6
            }

            if (ballIsMovingUp) {
                ballPosition.y -= 4; //10
            }
            if (ballIsMovingLeft) {
                ballPosition.x -= 4; //12
            }

            if (ballIsMovingRight) {
                ballPosition.x += 4; //12
            }


            //has ball hit the racket
            if (ballPosition.y + ballWidth >= (racketPosition.y - racketHeight / 2)) {
                int halfRacket = racketWidth / 2;

                if ((ballPosition.x + ballWidth > (racketPosition.x - halfRacket)) && (ballPosition.x - ballWidth < (racketPosition.x + halfRacket))) {
                    //play sound
                    score++;
                    ballIsMovingUp = true;
                    ballIsMovingDown = false;

                    if (ballPosition.x > racketPosition.x) {
                        ballIsMovingRight = true;
                        ballIsMovingLeft = false;

                    } else {
                        ballIsMovingRight = false;
                        ballIsMovingLeft = true;
                    }
                }
            }
        }

        public void drawCourt() {

            if (ourHolder.getSurface().isValid()) {
                canvas = ourHolder.lockCanvas();
                //Paint paint = new Paint();

                canvas.drawColor(Color.BLACK);
                paint.setColor(Color.argb(255, 255, 255, 255));
                paint.setTextSize(25);
                canvas.drawText("Score:" + score + " Lives:" + lives + " fps: " + fps, 20, 40, paint);

                //Draw the squash racket
                canvas.drawRect(racketPosition.x - (racketWidth / 2), racketPosition.y - (racketHeight / 2), racketPosition.x + (racketWidth / 2), racketPosition.y + racketHeight, paint);

                //Draw the ball
                canvas.drawRect(ballPosition.x, ballPosition.y, ballPosition.x + ballWidth, ballPosition.y + ballWidth, paint);

                ourHolder.unlockCanvasAndPost(canvas);

            }
        }



        public void controlFPS() {
            long timeThisFrame = (System.currentTimeMillis() - lastFrameTime);

            long timeToSleep = 15 - timeThisFrame;

            if (timeThisFrame > 0) {
                fps = (int) (1000 / timeThisFrame);
            }

            if (timeToSleep > 0) {

                try {
                    ourThread.sleep(timeToSleep);
                } catch (InterruptedException e) {

                }
            }

            lastFrameTime = System.currentTimeMillis();

        }


        public void pause() {

            playingSquash = false;
            try{
                ourThread.join();
            }catch (InterruptedException e){

            }

        }

        public void resume(){
            playingSquash = true;
            ourThread = new Thread(this);
            ourThread.start();
        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {

           switch (motionEvent.getAction() & motionEvent.ACTION_MASK){
               case MotionEvent.ACTION_DOWN:
                   if(motionEvent.getX() >= screenWidth / 2){
                       racketIsMovingRight = true;
                       racketIsMovingLeft = false;
                   }else {
                       racketIsMovingLeft = true;
                       racketIsMovingRight = false;
                   }
                   break;
               case MotionEvent.ACTION_UP:
                   racketIsMovingRight = false;
                   racketIsMovingLeft = false;
                   break;

           }
            return true;
        }
    }







}
