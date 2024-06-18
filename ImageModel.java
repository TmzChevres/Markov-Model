//Image Markov Model
//Tomaz Chevres
//June 6, 2024

import java.io.File;
import java.io.IOException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.util.*;

public class ImageModel {
    int order;
    BufferedImage image;
    Map<ModelPixel,Integer> pixels;
    boolean reorient = true; //rotate tile to minimize duplicates
    boolean manual = false; //only step when enter is pressed

    public static void main(String args[]){
        ImageModel model = new ImageModel("big.png",1);
        model.debugImage();
        model.generateImage(20, 20);
    }

    public ImageModel(String path, int k){
        order = k;
        int width=0, height=0;
        try {
            File input = new File(path);
            image = ImageIO.read(input);
            width = image.getWidth();
            height = image.getHeight();
        }
        catch(IOException e){
            e.printStackTrace();
        }
        System.out.println("Image loaded");

        pixels = new TreeMap<ModelPixel,Integer>();
        for(int x=0; x<width; x++){
            for(int y=0; y<height; y++){
                ModelPixel px = new ModelPixel(image, x, y, k, reorient);
                if(pixels.containsKey(px)){
                    pixels.put(px, pixels.get(px) + 1);
                }
                else {
                    pixels.put(px, 1);
                }
            }
        }

        //display all pixels
        try{
        System.out.println(pixels);
        } catch(OutOfMemoryError e){
            e.printStackTrace();
        }

        System.out.println("Found "+pixels.size() + " unique groups out of " + width*height + " total pixels ("+(100*pixels.size()/(width*height))+"%)");


    }

    public class ModelPixel implements Comparable<ModelPixel>{
        int color;
        int order;
        int[][] neighbors;
        boolean circular = true; //connect the edges of the image


        public ModelPixel(BufferedImage img, int x, int y, int k, boolean reorient){
            this(img,x,y,k,true,reorient);
        }
        public ModelPixel(BufferedImage img, int x, int y, int k, boolean overrideAlpha, boolean reorient){
            order = k;
            neighbors = new int[2*k+1][2*k+1];
            int width = img.getWidth();
            int height = img.getHeight();
            
            for(int i=0; i<=2*k; i++){
                for(int j=0; j<=2*k; j++){
                    if(circular || (0<=x-k+i && x-k+i<width && 0<=y-k+j && y-k+j<height))
                        neighbors[i][j] = img.getRGB((x-k+i+width)%width,(y-k+j+height)%height);
                        if(overrideAlpha && ((neighbors[i][j]>>24)&0xFF)==0) neighbors[i][j]=0; //sets all pixels with 0 alpha to 0x00000000
                }
            }
            color = neighbors[k][k];
            
            /* Reorients the image to prevent duplicates
             *  Generate all 8 varients (4 rotations x 2 mirrors)
             *  Sort and keep the first one
             */
            if(reorient){
                //generate all orientations
                int[][][] orientations = new int[8][][];
                orientations[0] = neighbors;
                orientations[1] = mirror(neighbors);
                for(int i=1; i<4; i++){
                    orientations[2*i] = rotate(neighbors, i);
                    orientations[2*i+1] = mirror(orientations[2*i]);
                }

                int max = 0;
                for(int i=1; i<orientations.length; i++){
                    int compare = 0;
                    int j = 0;
                    while(compare==0 && j<(2*order+1)*(2*order+1)){
                        compare = orientations[i][j%(2*order+1)][j/(2*order+1)] - orientations[max][j%(2*order+1)][j/(2*order+1)];
                        j++;
                    }
                    if(compare>0) max = i;
                }
                neighbors = orientations[max];

                //print all
                // for(int i=0; i<8; i++){
                //     System.out.println(i);
                //     for(int row=0; row<2*order+1; row++){
                //         for(int col=0; col<2*order+1; col++){
                //             System.out.print(Integer.toHexString(orientations[i][col][row])+" ");
                //         }
                //         System.out.println();
                //     }
                // }
            }
        }

        public int compareTo(ModelPixel mp){
            int compare = 0;
            int i = 0;
            while(compare == 0 && i<(2*order+1)*(2*order+1)){
                compare = neighbors[i%(2*order+1)][i/(2*order+1)] - mp.neighbors[i%(2*order+1)][i/(2*order+1)];
                i++;
            }
            return compare;
        }

        public boolean equals(ModelPixel mp){
            for(int i=0; i<(2*order+1)*(2*order+1); i++){
                if(neighbors[i%(2*order+1)][i/(2*order+1)] != mp.neighbors[i%(2*order+1)][i/(2*order+1)]) return false;
            }
            return true;
        }

        public int[][] rotate(int[][] arr, int n){
            int size = arr.length;
            n%=4;
            int[][] out = new int[size][size];
            out[size/2][size/2] = arr[size/2][size/2];

            for(int i=0; i<(size+1)/2; i++){ //x
                for(int j=0; j<size/2; j++){ //y
                    int x=i,y=j;
                    if(n==1){
                        x=j;
                        y=size-1-i;
                    }
                    else if(n==2){
                        x=size-1-i;
                        y=size-1-j;
                    }
                    else if(n==3){
                        x=size-1-j;
                        y=i;
                    }

                    out[x][y] = arr[i][j];
                    out[size-1-y][x] = arr[size-1-j][i];
                    out[size-1-x][size-1-y] = arr[size-1-i][size-1-j];
                    out[y][size-1-x] = arr[j][size-1-i];
                }
            }

            return out;
        }
        
        public int[][] mirror(int[][] arr){
            int[][] out = new int[arr.length][arr.length];
            for(int x=0; x<arr.length; x++){
                for(int y=0; y<arr.length; y++){
                    out[x][y] = arr[y][x];
                }
            }
            return out;
        }

        public String toString(){
            String out = "\n";
            for(int y=0; y<neighbors[0].length; y++){
                out+="\n";
                for(int x=0; x<neighbors.length; x++){
                    out+= colorString("\u2588",neighbors[x][y]);
                }
            }
            return out;
        }

        public static void print(int[][] neighbors){
            for(int y=0; y<neighbors[0].length; y++){
                for(int x=0; x<neighbors.length; x++){
                    System.out.print(neighbors[x][y]+" ");
                }
                System.out.println();
            }
        }

    }

    public void generateImage(int width, int height){
        BufferedImage gen = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for(int i=0; i<width*height; i++) gen.setRGB(i%width,i/height,0x00FF00FF); //set entire background to (invisible) magenta - used to differentiate ungenerated pixels vs transparent pixels
        
        TreeMap<Integer,GenPixel> check = new TreeMap<Integer,GenPixel>();

        //set starting pixel
        // gen.setRGB(width/2,height/2,image.getRGB((int)(Math.random()*image.getWidth()),(int)(Math.random()*image.getHeight())));
        gen.setRGB(width/2,height/2,image.getRGB(0,0)); //TEST ONLY - USE RANDOM FOR NORMAL USE
        for(int x=width/2-order; x<=width/2+order; x++){
            for(int y=height/2-order; y<=height/2+order; y++){
                if(0<=x && x<width && 0<=y && y<height && (x!=width/2 || y!=height/2)){
                    check.put(x+y*gen.getWidth(), new GenPixel(gen, x, y, order, reorient));
                }
            }
        }
        

        //generate pixels
        try{
            //while there are more pixels to check
            int complete = 0;

            while(check.size()>0 && (!manual || System.in.read() == 10)){
            // while(check.size()>0){
                //get next pixel (make more random later???)
                int minPos = 0;
                int i=(2*order+1)*(2*order+1)-1;
                GenPixel min = null;
                while(min == null){
                    for(Integer pos:check.keySet()){
                        if(min == null || min.getCount(i)==0 || (0<check.get(pos).getCount(i) && check.get(pos).getCount(i)<min.getCount(i))){
                            min = check.get(pos);
                            minPos = pos;
                        }
                    }
                    if(min.getCount(i)==0){
                        min = null;
                        i--;
                    }
                }
                
                // System.out.println(minPos+" "+i+" "+min.getCount(i));
                
                //set color
                gen.setRGB(minPos%gen.getWidth(), minPos/gen.getWidth(),min.generate(i));
                check.remove(minPos);

                //update check
                for(int dx=-order; dx<=order; dx++){
                    for(int dy=-order; dy<=order; dy++){
                        int x = minPos%gen.getWidth();
                        int y = minPos/gen.getHeight();
                        if(0<=x+dx && x+dx<gen.getWidth() && 0<=y+dy && y+dy<gen.getHeight()){
                            if(check.containsKey(minPos+dx+dy*gen.getWidth())){//if position already exists, update
                                check.get(minPos+dx+dy*gen.getWidth()).update(true);
                            }
                            else if(gen.getRGB(x+dx,y+dy)==0x00FF00FF){//if not & empty, create new
                                check.put(minPos+dx+dy*gen.getWidth(), new GenPixel(gen, x+dx, y+dy, order, reorient));
                            }
                        }
                    }
                }
                

                //print percent (when it changes)
                complete++;
                if(manual || 100*complete/(width*height) != 100*(complete-1)/(width*height)){
                    System.out.println(complete+" "+100*complete/(width*height)+"%");
                }
                
                //draw image (if manual) - DEBUG
                if(manual){
                    BufferedImage copy = new BufferedImage(gen.getWidth(), gen.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    copy.setRGB(0, 0, copy.getWidth(), copy.getHeight(), gen.getRGB(0, 0, gen.getWidth(), gen.getHeight(), null, 0, gen.getWidth()), 0, copy.getWidth());
                    for(Integer unchecked:check.keySet()){
                        //green = less
                        if(check.get(unchecked).getCount((2*order+1)*(2*order+1)-1)==0) copy.setRGB(unchecked%gen.getWidth(),unchecked/gen.getHeight(),0xFF0000FF);
                        else copy.setRGB(unchecked%gen.getWidth(),unchecked/gen.getHeight(),gradient(0xFF00FF00, 0xFFFF0000, 1.0f*check.get(unchecked).getCount((2*order+1)*(2*order+1)-1)/pixels.size()));
                    }
                    try{
                        ImageIO.write(copy, "png", new File("generate.png"));
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }

            //DEBUG - if stopped early
            if(check.size()>0){
                //set unfilled checks to color
                for(Integer i:check.keySet()){
                    //green = less
                    gen.setRGB(i%gen.getWidth(),i/gen.getHeight(),gradient(0xFF00FF00, 0xFFFF0000, 1.0f*check.get(i).getCount((2*order+1)*(2*order+1)-1)/pixels.size()));
                }
                System.out.print(pixels.size()+" ");
                for(int i=100; i>=0; i--){
                    System.out.print(colorString("\u2588",gradient(0xFF00FF00, 0xFFFF0000, 1.0f*i/100)));
                }
                System.out.println(" 0");
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }

        try{
            ImageIO.write(gen, "png", new File("generate.png"));
        }
        catch(IOException e){
            e.printStackTrace();
        }

    }

    private class GenPixel extends ModelPixel{
        ArrayList<ArrayList<ModelPixel>> match;
        BufferedImage image;
        int x,y;

        public GenPixel(BufferedImage img, int x, int y, int k, boolean reorient){
            super(img,x,y,k,false,false);
            image = img;
            this.x = x;
            this.y = y;
            match = new ArrayList<ArrayList<ModelPixel>>();
            
            for(int i=0; i<(2*order+1)*(2*order+1); i++){
                match.add(new ArrayList<ModelPixel>());
            }

            update(false);
        }

        public void update(boolean updateNeighbors){
            //reset match
            for(ArrayList<ModelPixel> list:match){
                list.clear();
            }

            if(updateNeighbors){
                int width = image.getWidth();
                int height = image.getHeight();
                for(int i=0; i<=2*order; i++){
                    for(int j=0; j<=2*order; j++){
                        if(circular || (0<=x-order+i && x-order+i<width && 0<=y-order+j && y-order+j<height))
                            neighbors[i][j] = image.getRGB((x-order+i+width)%width,(y-order+j+height)%height);
                    }
                }
                color = neighbors[order][order];
            }

            //count the number of corresponding colors
            for(ModelPixel mp:pixels.keySet()){
                int[] count = new int[8]; //running match count for each orientation
                for(int row=0; row<2*order+1; row++){
                    for(int col=0; col<2*order+1; col++){
                        if(row!=order || col!=order){
                            if(neighbors[col][row] == 0x00FF00FF){
                                for(int i=0; i<count.length; i++) count[i]++;//0x00FF00FF matches all
                            }
                            else if(reorient){
                                if(neighbors[col][row] == mp.neighbors[col][row]) count[0]++; //standard
                                if(neighbors[col][row] == mp.neighbors[2*order-row][col]) count[1]++; //rotated 90
                                if(neighbors[col][row] == mp.neighbors[2*order-col][2*order-row]) count[2]++; //rotated 180
                                if(neighbors[col][row] == mp.neighbors[row][2*order-col]) count[3]++; //rotated 270

                                if(neighbors[col][row] == mp.neighbors[row][col]) count[4]++; //mirrored
                                if(neighbors[col][row] == mp.neighbors[col][2*order-row]) count[5]++; //rotated 90 & mirrored
                                if(neighbors[col][row] == mp.neighbors[2*order-row][2*order-col]) count[6]++; //rotated 180 & mirrored
                                if(neighbors[col][row] == mp.neighbors[2*order-col][row]) count[7]++; //rotated 270 & mirrored
                            } else {
                                if(neighbors[col][row] == mp.neighbors[col][row]) count[0]++; //standard (no reorientation)
                            }
                        }
                    }
                }

                //get greatest count
                int countMax=count[0];
                for(int i=1; i<count.length; i++){
                    if(count[i]>countMax){
                        countMax = count[i];
                    }
                }

                //set
                match.get(countMax).add(mp);
            }

            // for(int i=0; i<match.size(); i++){
            //     System.out.print(match.get(i).size()+"\t");
            // }
            // System.out.println();

        }

        public int getCount(int i){
            return match.get(i).size();
        }

        public int generate(int i){
            ArrayList<ModelPixel> matchPixels = match.get(i);

            //select random tile (weighted by appearences)
            int sum = 0;
            for(ModelPixel p:matchPixels){
                sum+=pixels.get(p);
            }
            if(sum==0) return 0;

            sum*=Math.random();
            int select = -1;
            while(sum>=0){
                select++;
                sum-=pixels.get(matchPixels.get(select));
                // System.out.println(matchPixels.get(select)+"\n"+select);
            }
            

            return matchPixels.get(select).color;
        }
    }
    


    public void debugImage(){
        int margin = 1;

        int size = 2*order+1;
        BufferedImage debugImage = new BufferedImage((int)Math.ceil(Math.sqrt(pixels.size())) * (size+margin) - margin, (int)Math.ceil(Math.sqrt(pixels.size())) * (size+margin) - margin, BufferedImage.TYPE_INT_ARGB);
        System.out.println("Debug image size: "+debugImage.getWidth()+"x"+debugImage.getHeight());

        int x=0, y=0;
        for(ModelPixel mp:pixels.keySet()){
            for(int dx=0; dx<size; dx++){
                for(int dy=0; dy<size; dy++){
                    debugImage.setRGB(x+dx,y+dy,mp.neighbors[dx][dy]);
                }
            }
            x+=size+margin;
            if(x>=debugImage.getWidth()){
                x=0;
                y+=size+margin;
            }
        }
        try{
            ImageIO.write(debugImage, "png", new File("debug.png"));
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }



    public static int[] getRGB(int clr){//returns rgb as an array
        return new int[]{(clr>>16)&0xFF,(clr>>8)&0xFF,clr&0xFF};
    }

    public static String colorString(String str, int num){
        return "\u001B[38;2;"+(num>>16&0xFF)+";"+(num>>8&0xFF)+";"+(num&0xFF)+"m"+str+"\u001B[0m";
    }

    public static int gradient(int c1, int c2, float p){
        return ((int)((c1>>24&0xFF)+((c2>>24&0xFF)-(c1>>24&0xFF))*p)<<24)
             | ((int)((c1>>16&0xFF)+((c2>>16&0xFF)-(c1>>16&0xFF))*p)<<16)
             | ((int)((c1>> 8&0xFF)+((c2>> 8&0xFF)-(c1>> 8&0xFF))*p)<< 8)
             | ((int)((c1    &0xFF)+((c2    &0xFF)-(c1    &0xFF))*p));
    }

}