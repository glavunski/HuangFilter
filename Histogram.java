import java.util.*;

public class Histogram{
    private List<Integer> sortedBinsRed = new ArrayList<>();
    private List<Integer> sortedBinsGreen = new ArrayList<>();
    private List<Integer> sortedBinsBlue = new ArrayList<>();

    public Histogram(){
        this.sortedBinsRed = new ArrayList<>();
        this.sortedBinsGreen = new ArrayList<>();
        this.sortedBinsBlue = new ArrayList<>();
    }

    public void incrementLevelFrequency(int r,int g,int b){
        this.sortedBinsRed.add(r);
        this.sortedBinsGreen.add(g);
        this.sortedBinsBlue.add(b);
        Collections.sort(sortedBinsRed);
        Collections.sort(sortedBinsGreen);
        Collections.sort(sortedBinsBlue);
    }

    public void decrementLevelFrequency(int r,int g,int b){
        int indexR = Collections.binarySearch(sortedBinsRed,r);
        int indexG = Collections.binarySearch(sortedBinsGreen,g);
        int indexB = Collections.binarySearch(sortedBinsBlue,b);

        this.sortedBinsRed.remove(indexR);
        this.sortedBinsGreen.remove(indexG);
        this.sortedBinsBlue.remove(indexB);

        Collections.sort(sortedBinsRed);
        Collections.sort(sortedBinsGreen);
        Collections.sort(sortedBinsBlue);
    }

    public int[] getMedian(){
        int red = this.getMedian(sortedBinsRed);
        int green = this.getMedian(sortedBinsGreen);
        int blue = this.getMedian(sortedBinsBlue);

        return new int[]{red,green,blue};
    }

    private int getMedian(List<Integer> l){
        int count = l.size();
        Integer[] arr = l.toArray(new Integer[count]);
        if(count > 2){
            if(count % 2 == 0){
                return (arr[count / 2 - 1] + arr[count / 2]) / 2;
            }else{
                return arr[count / 2];
            }
        }else if(count > 0){
            return arr[count / 2 + 1];
        }else{
            return 0;
        }
    }
}


