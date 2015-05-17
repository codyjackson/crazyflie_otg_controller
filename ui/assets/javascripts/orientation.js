function radians(degrees){
    return degrees*Math.PI/180.0;
}

function degrees(radians){
    return radians*180.0/Math.PI;
}

function Orientation(pitch, roll, yaw) {
    this.pitch = pitch;
    this.roll = roll;
    this.yaw = yaw;
}

Orientation.prototype.rotateYaw = function (angle) {
    var v = Vector.create([this.pitch, this.roll, 0]);
    var m = Matrix.RotationZ(radians(angle));
    var v2 = m.multiply(v);
    return new Orientation(v2.e(1), v2.e(2), this.yaw + angle);
};

module.exports = Orientation;