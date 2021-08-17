# FishingNote
This application provides an environment where users can measure and manage fish without a separate measuring tool. It manages date, measurement data, location, etc. and displays it on the map.


## ARCore anchor has 3d mapping values
* finger tapped point has real 3d location info of x, y, z
* first tapped point is the start location of measuring point
* second tapped point is the end location of measuring point
* caculates the distance as below
```
(x1-x2)^2 + (y1-y2)^2 + (z1-z2)^2 = d^2
```
* third tapped event initialize the previous tapped points

## FishingNote main usage
* measure fish length by ARCore
* save the fish data to local db
  * fish type, length, location ... etc
* provides map view with the marker in the measured location
  * measured location is the place where app user saved picture by FishingNote camera. 
  * FishingNote calculates current location when taking a picture

## measuring activity
![20210817233314_screenshot](https://user-images.githubusercontent.com/42398891/129749209-10d47c52-bbf7-4078-a195-b6277bc51dc8.jpg)

## saving activity
![Screenshot_20210817-233322_AndroidArcoreDistanceCamera](https://user-images.githubusercontent.com/42398891/129749556-cc31ba37-17b5-474f-8296-6ae4eacdf0f7.jpg)

## load mapview by parsing db and add marker 
![Screenshot_20210817-233351_AndroidArcoreDistanceCamera](https://user-images.githubusercontent.com/42398891/129749668-04b9688b-8284-4386-954c-a878a4dca1cd.jpg)




