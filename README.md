# ARCoreMeasure
## ARCore anchor has 3d mapping values
* finger tapped point has real 3d location info of x, y, z
* first tapped point is the start location of measuring point
* second tapped point is the end location of measuring point
* caculates the distance as below
```
(x1-x2)^2 + (y1-y2)^2 + (z1-z2)^2 = d^2
```
* third tapped event initialize the previous tapped points
