package com.algorithm.sort;

//不具备稳定性
public class HeapSort {
    public static void heapSort(int[] arr){
        if(arr==null || arr.length<2){
            return;
        }
        //把数组整体范围内搞成大根堆
        for(int i=0;i<arr.length;i++){
            heapInsert(arr,i);
        }
        int heapSize=arr.length;
        swap(arr,0,--heapSize);
        while (heapSize>0){//确切的来说从这个地方开始排序的
            heapify(arr,0,heapSize);
            swap(arr,0,--heapSize);
        }
    }

    public static void swap(int[] arr, int l, int r) {
        int tmp=arr[l];
        arr[l] =arr[r];
        arr[r]=tmp;
    }

    //某个数现在处于index位置，往上继续移动
    public static void heapInsert(int[] arr,int index){
        while (arr[index]>arr[(index-1)/2]){//当子结点比父结点大的时候
            swap(arr,index,(index-1)/2);
        }
    }

    //某个数在index位置 能否往下移动
    public static void heapify(int[] arr,int index,int heapSize){
        int left=index*2+1;//左孩子的下标
        while (left<heapSize){//下方还有孩子的时候
            //两个孩子中谁的值大就将下标给largest  left+1就是右孩子
            int largest=left+1<heapSize && arr[left+1]>arr[left] ? left+1 :left;
            //父和较大的孩子之间谁的值大把下标给largest
            largest=arr[largest]>arr[index] ? largest :index;
            if(largest==index){
                break;
            }
            swap(arr,largest,index);
            index=largest;
            left=index*2+1;
        }
    }
}
