package com.algorithm.sort;

import java.util.Arrays;

public class InsertSort {
    public static void insertionSort(int[] arr){
        if(arr==null || arr.length<2){
            return;
        }
        //0~0有序 0~N想有序
        for(int i=1;i<arr.length;i++){
            for(int j=i-1;j>=0 && arr[j]>arr[j+1];j--){
                swap(arr,j,j+1);
            }
        }
    }

    public static void swap(int[] arr, int j, int i) {
        arr[i]=arr[i] ^ arr[j];
        arr[j]=arr[i] ^ arr[j];
        arr[i]=arr[i] ^ arr[j];
    }
}
