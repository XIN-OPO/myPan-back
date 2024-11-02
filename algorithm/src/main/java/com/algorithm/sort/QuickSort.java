package com.algorithm.sort;

//快排
//不具备稳定性
public class QuickSort {
    public static void quickSort(int[] arr){
        if(arr==null || arr.length<2){
            return;
        }
        quickSort(arr,0,arr.length-1);
    }

    public static void quickSort(int[] arr,int l,int r){
        if(l<r){
            swap(arr,l+(int)(Math.random()*(r-l+1)),r);//隨機一個輸和最後一個交換
            int[] p=partition(arr,l,r);//劃分小中大區域
            quickSort(arr, l, p[0]-1);
            quickSort(arr, p[1]+1, r);
        }
    }

    //这是一个处理arr[l..r]的函数
    //默认以arr[r]做划分 arr[r] -> p < p =p >p
    //返回等于区域的左边界和右边界 返回长度一定等于二
    public static int[] partition(int[] arr,int l ,int r){
        int less=l-1;//小于区域的右边界
        int more=r;//大于区域的左边界
        while(l<more){//l表示当前数的位置 arr[r] = > 划分值
            if(arr[l]<arr[r]){//当前数 < 划分值
                swap(arr,++less,l++);
            }else if(arr[l]>arr[r]){
                swap(arr,--more,l);
            }else {
                l++;//等于就下标++
            }
        }
        swap(arr,more,r);
        return new int[] {less+1,more};//返回左邊界和右邊界
    }

    public static void swap(int[] arr, int l, int r) {
        int tmp=arr[l];
        arr[l] =arr[r];
        arr[r]=tmp;
    }
}


