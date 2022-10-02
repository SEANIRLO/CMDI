# CMDI
## Introduction
* This is an Undergraduates’ Innovation and Entrepreneurship Training Program of China 2021, which aimed at developing an object detection model based on CV algorithms that detect Baseband Unit (BBU) devices in base stations from images, devising an automatic acceptance check architecture & procedure and deploying the model to mobile devices, in order to fully automate the acceptance check of base stations.
* This project is jointly instructed by BUPT and CMCC (China Mobile Communications Group Co., Ltd.). (© 2022 CMCC. All rights reserved.)
* This project was awarded the 'Municipal Level Project'. (the second best grade)

## Overview
* We cleaned the BBU image dataset provided by CMCC and innovatively designed an offline-augmentation pipeline.
* We conducted model generation and assessment based on 5-fold cross validation. The performance of our model was satisfying.
* We exported and deployed our BBU detection model to mobile devices withAndroid OS.

## Model Generation and Assessment
In consideration of data privacy of CMCC, we are sorry that complete codes are not available. However, we provide several jupyter notebooks that recorded our preprocessing operations. We recommend interested audiences to visit [English version](https://wandb.ai/seanirlo/YOLOv5/reports/CMDI-Report--VmlldzoyNzI4NDQ4/edit) or [Chinese version](https://wandb.ai/seanirlo/YOLOv5/reports/-BBU---VmlldzoyMDIyMDI1) for the full report of model generation and assessment.

## Model Deployment
In consideration of technical privacy of CMCC, we are sorry that the final model is not availbale. However, we provide source code of our APP without the model. Our APP was built on the basis of [nihui/ncnn-android-yolov5](https://github.com/nihui/ncnn-android-yolov5) and [Tencent/ncnn](https://github.com/Tencent/ncnn).

## Video

https://user-images.githubusercontent.com/59157711/193447603-0ef230b9-0c9e-4b48-8a99-d4750511419b.mp4
