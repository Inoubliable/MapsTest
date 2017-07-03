package com.janzelj.tim.mapstest;

/**
 * Created by mitja on 5/16/17...
 */

class Maths {

    static float xRatio;
    static float yRatio;

    static double[] getCWmatrix(double rotation){

        double rotationInRadians = Math.toRadians(rotation);

        return new double[]{Math.cos(rotationInRadians),     //  /  cos()     -sin() \
                            Math.sin(rotationInRadians),     //  |                   |
                         -1* Math.sin(rotationInRadians),     //  |                   |
                            Math.cos(rotationInRadians)};    //  \  sin()      cos() /
    }

    static double[] getCCWmatrix(double rotation){

        double rotationInRadians = Math.toRadians(rotation);

        return new double[]{Math.cos(rotationInRadians),   //  /  cos()      sin() \
                         -1* Math.sin(rotationInRadians),   //  |                   |
                            Math.sin(rotationInRadians),   //  |                   |
                            Math.cos(rotationInRadians)};  //  \ -sin()      cos() /
    }

    static double[] mulMatrixVector(double[] matrix, double[] vector){

        return new double[]{(matrix[0]*vector[0])+(matrix[1]*vector[1]),
                            (matrix[2]*vector[0])+(matrix[3]*vector[1])};
    }

    static double[] sumVectorVector(double[] vector1, double[] vector2){

        return new double[]{vector1[0]+vector2[0], vector1[1]+vector2[1]};
    }

    static double[] subVectorVector(double[] vector1, double[] vector2){

        return new double[]{vector1[0]-vector2[0], vector1[1]-vector2[1]};
    }

    public static double[] reverseVector(double[] original){

        return new double[]{-1*original[0], -1*original[1]};
    }

    static double[] vectorToUintVector(double[] vector){
        double[] unitVetor = {0,0};

        double magnitute = getVectorMagnitute(vector);

        unitVetor[0] = vector[0]/magnitute;
        unitVetor[1] = vector[1]/magnitute;

        return unitVetor;
    }

    static double[] mulConstantVector(double[] vector, double constant){

        return new double[]{vector[0]*constant, vector[1]*constant};
    }

    static double getVectorMagnitute(double[] vector){
        return Math.sqrt(Math.pow(vector[0],2) + Math.pow(vector[1],2));
    }
}
