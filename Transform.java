public class Transform {
    public static void main(String args[]){
        int size = 4 ;
        int[][] arr = new int[size][size];

        for(int x=0; x<size; x++){
            for(int y=0; y<size; y++){
                arr[x][y] = x + y*size;
            }
        }

        // print(arr);
        // System.out.println();

        // print(rotate(arr, 1));
        // System.out.println();
        // print(rotate(arr, 2));
        // System.out.println();
        // print(rotate(arr, 3));
        // System.out.println();
        // print(rotate(arr, 4));
        // System.out.println();

    }

    /*
    public static void rotate(int[][] arr, int n){
        int size = arr.length;
        n%=4;
        int[] temp = new int[4];

        if(n>0){
            for(int i=0; i<(size+1)/2; i++){ //x
                for(int j=0; j<size/2; j++){ //y
                    int x=0,y=0;
                    if(n==1){
                        x=size-1-j;
                        y=i;
                    }
                    else if(n==2){
                        x=size-1-i;
                        y=size-1-j;
                    }
                    else if(n==3){
                        x=j;
                        y=size-1-i;
                    }

                    temp[0] = arr[x][y];
                    temp[1] = arr[size-1-y][x];
                    temp[2] = arr[size-1-x][size-1-y];
                    temp[3] = arr[y][size-1-x];
                    arr[x][y] = arr[i][j];
                    arr[size-1-y][x] = arr[size-1-j][i];
                    arr[size-1-x][size-1-y] = arr[size-1-i][size-1-j];
                    arr[y][size-1-x] = arr[j][size-1-i];
                    arr[i][j] = temp[0];
                    arr[size-1-j][i] = temp[1]; 
                    arr[size-1-i][size-1-j] = temp[2];
                    arr[j][size-1-i] = temp[3];
                }
            }
        }
    }

    public static void mirror(int[][] arr){
        for(int y=0; y<arr.length; y++){
            for(int x=y+1; x<arr.length; x++){
                int temp = arr[y][x];
                arr[y][x] = arr[x][y];
                arr[x][y] = temp;
            }
        }
    }
    */
    
    public static int[][] rotate(int[][] arr, int n){
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
    
    public static int[][] mirror(int[][] arr){
        int[][] out = new int[arr.length][arr.length];
        for(int y=0; y<arr.length; y++){
            for(int x=y+1; x<arr.length; x++){
                out[y][x] = arr[x][y];
                out[x][y] = arr[y][x];
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

    public static String colorString(String str, int num){
        return "\u001B[38;2;"+(num>>16&0xFF)+";"+(num>>8&0xFF)+";"+(num&0xFF)+"m"+str+"\u001B[0m";
    }
}
