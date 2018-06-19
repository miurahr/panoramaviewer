package tokyo.northside.mapillary;

import java.util.stream.IntStream;

class VectorUtil {
    private static final double ACCURACY_FACTOR = 2048;
    private static final int REQUIRED_SIZE = (int) (2 * ACCURACY_FACTOR);
    private double[] asinTable;
    private double[] atan2Table;
    private Vector3D[][] vectors;
    private Rotation rotation;

    static final double INV_PI = 1 / Math.PI;
    static final double INV_2PI = 1 / (2 * Math.PI);

    VectorUtil() {
        asinTable = new double[REQUIRED_SIZE];
        atan2Table = new double[REQUIRED_SIZE * REQUIRED_SIZE];
        IntStream.range(0, REQUIRED_SIZE).forEach(i -> {
            asinTable[i] = Math.asin((i - ACCURACY_FACTOR) * 1 / ACCURACY_FACTOR);
            IntStream.range(0, REQUIRED_SIZE).forEach(j -> {
                double y = (i - ACCURACY_FACTOR) / ACCURACY_FACTOR;
                double x = (j - ACCURACY_FACTOR) / ACCURACY_FACTOR;
                atan2Table[i + j * REQUIRED_SIZE] = Math.atan2(y, x);
            });
        });
        rotation = new Rotation(0, 0);
    }

    void setCameraScreen(int width, int height, double d) {
        vectors = new Vector3D[width][height];
        IntStream.range(0, height).forEach(y -> {
            IntStream.range(0, width).forEach(x -> {
                double vecX = x - width / 2;
                double vecY = y - height / 2;
                double vecZ = d;
                double invVecLength = 1 / Math.sqrt(vecX * vecX + vecY * vecY + vecZ * vecZ);
                vectors[x][y] = new Vector3D(vecX * invVecLength, vecY * invVecLength, vecZ * invVecLength);
            });
        });
    }

    double atan2(double x, double z) {
        int iX = (int) ((x + 1) * ACCURACY_FACTOR);
        int iZ = (int) ((z + 1) * ACCURACY_FACTOR);
        return atan2Table[iZ + iX * REQUIRED_SIZE];
    }

    double asin(double y) {
        int iY = (int) ((y + 1) * ACCURACY_FACTOR);
        return asinTable[iY];
    }

    Vector3D getVector3D(int x, int y) {
        return rotation.rotate(vectors[x][y]);
    }

    void setRotationDelta(double deltaTheta, double deltaPhi) {
        rotation.setDelta(deltaTheta, deltaPhi);
    }
}
