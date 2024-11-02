package com.algorithm.sort;

//冒泡排序
public class BubbleSort {
    public static void bubbleSort(int[] arr){
        if(arr==null || arr.length<2){
            return;
        }
        for(int e=arr.length-1;e>0;e--){
            for(int i=0;i<e;i++){
                if(arr[i]>arr[i+1]){
                    swap(arr,i,i+1);
                }
            }
        }
    }

    public static void swap(int[] arr, int i, int j) {
        arr[i]=arr[i]^arr[j];//抑或运算 亦或还可以理解为无进位相加
        arr[j]=arr[i]^ arr[j];
        arr[i]=arr[i]^arr[j];
    }
}
