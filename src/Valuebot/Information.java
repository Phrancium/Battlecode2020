package Valuebot;

import java.util.Objects;

public class Information implements Comparable {


    private int type;
    private int x;
    private int y;

    public int getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Information(int type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
    }

    @Override
    public int compareTo(Object o) {
        return type - ((Information)o).getType();
    }

    @Override
    public String toString() {
        return "Information{" +
                "type=" + type +
                ", x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Information that = (Information) o;
        return type == that.type &&
                x == that.x &&
                y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, x, y);
    }
}
