package com.drppp.drtech.lootgames.minigame.minesweeper;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.drppp.drtech.lootgames.api.util.NBTUtils;
import com.drppp.drtech.lootgames.api.util.Pos2i;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MSBoard {
    private MSField[][] board;
    private int size;
    private int bombCount;

    @SideOnly(Side.CLIENT)
    int cFlaggedFields;

    public MSBoard(int size, int bombCount) {
        this.size = size;
        this.bombCount = bombCount;
    }

    @SideOnly(Side.CLIENT)
    void updateFlaggedFields_c() {
        cFlaggedFields = 0;
        for (MSField[] msFields : board) {
            for (MSField msField : msFields) {
                if (msField != null && msField.getMark() == MSField.FLAG) {
                    cFlaggedFields++;
                }
            }
        }
    }

    public boolean isGenerated() { return board != null; }

    void reveal(Pos2i pos) { getField(pos).reveal(); }
    void reveal(int x, int y) { getField(x, y).reveal(); }

    public boolean isHidden(Pos2i pos) { return getField(pos).isHidden(); }
    public boolean isHidden(int x, int y) { return getField(x, y).isHidden(); }

    public int getType(Pos2i pos) { return getField(pos).getType(); }
    public int getType(int x, int y) { return getField(x, y).getType(); }

    void swapMark(Pos2i pos) { getField(pos).swapMark(); }

    public int getMark(Pos2i pos) { return getField(pos).getMark(); }
    public int getMark(int x, int y) { return getField(x, y).getMark(); }

    void resetBoard() { resetBoard(size, bombCount); }

    void resetBoard(int newBoardSize, int newBombCount) {
        size = newBoardSize;
        bombCount = newBombCount;
        board = null;
        cFlaggedFields = 0;
    }

    boolean isBomb(int x, int y) { return getType(x, y) == MSField.BOMB; }
    boolean isBomb(Pos2i pos) { return getType(pos) == MSField.BOMB; }

    public boolean hasFieldOn(Pos2i pos) {
        return pos.getX() >= 0 && pos.getY() >= 0 && pos.getX() < size && pos.getY() < size;
    }

    boolean checkWin() {
        for (MSField[] msFields : board) {
            for (MSField msField : msFields) {
                if (msField.type == MSField.BOMB) {
                    if (!msField.isHidden || msField.getMark() != MSField.FLAG) return false;
                } else {
                    if (msField.isHidden) return false;
                }
            }
        }
        return true;
    }

    private MSField getField(Pos2i pos) {
        return board[pos.getX()][pos.getY()];
    }
    private MSField getField(int x, int y) {
        return board[x][y];
    }

    public void generate(Pos2i startFieldPos) {
        if (convertToFieldIndex(startFieldPos) > (size * size) - 1) {
            throw new IllegalArgumentException("Start Pos must be strictly less than Board size");
        }

        board = new MSField[size][size];
        int square = size * size;

        ArrayList<Integer> fields = new ArrayList<>(square);
        for (int i = 0; i < square; i++) {
            if (i == convertToFieldIndex(startFieldPos)) continue;
            fields.add(i);
        }
        Collections.shuffle(fields);

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = new MSField(-2, true, MSField.NO_MARK);
            }
        }

        for (Integer integer : fields.subList(0, bombCount)) {
            board[integer % size][integer / size].type = MSField.BOMB;
        }

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (!isBomb(i, j)) {
                    board[i][j].type = getConnectedBombCount(i, j);
                }
            }
        }

        getField(startFieldPos).reveal();
    }

    private int convertToFieldIndex(Pos2i pos) {
        return pos.getY() * size + pos.getX();
    }

    private int getConnectedBombCount(int x, int y) {
        int bombCount = 0;
        for (int i = -1; i <= 1; i++) {
            int xCoord = x + i;
            if (xCoord >= 0 && xCoord < size()) {
                for (int j = -1; j <= 1; j++) {
                    int yCoord = y + j;
                    if (yCoord >= 0 && yCoord < size()) {
                        if (isBomb(xCoord, yCoord)) bombCount++;
                    }
                }
            }
        }
        return bombCount;
    }

    public void forEach(Consumer<Pos2i> func) {
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[x].length; y++) {
                func.accept(new Pos2i(x, y));
            }
        }
    }

    public void forEach(BiConsumer<Integer, Integer> func) {
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[x].length; y++) {
                func.accept(x, y);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    void setField(Pos2i pos, int type, boolean hidden, int mark) {
        if (board == null) return;
        MSField oldField = board[pos.getX()][pos.getY()];
        int oldMark = oldField != null ? oldField.mark : MSField.NO_MARK;
        if (!hidden) mark = MSField.NO_MARK;
        board[pos.getX()][pos.getY()] = new MSField(type, hidden, mark);
        if (oldMark == MSField.FLAG && mark != MSField.FLAG) cFlaggedFields--;
        else if (oldMark != MSField.FLAG && mark == MSField.FLAG) cFlaggedFields++;
    }

    void setBoard(MSField[][] board) { this.board = board; }
    public int size() { return size; }
    public int getBombCount() { return bombCount; }
    void setBombCount(int bombCount) { this.bombCount = bombCount; }

    @SideOnly(Side.CLIENT)
    public int getFlaggedField_c() { return cFlaggedFields; }

    void setSize(int size) { this.size = size; }

    NBTTagCompound writeNBTForSaving() {
        return NBTUtils.writeTwoDimArrToNBT(board);
    }

    NBTTagCompound writeNBTForClient() {
        return NBTUtils.writeTwoDimArrToNBT(board, field -> {
            NBTTagCompound c = new NBTTagCompound();
            c.setBoolean("hidden", field.isHidden());
            c.setInteger("mark", field.getMark());
            if (!field.isHidden()) {
                c.setInteger("type", field.getType());
            }
            return c;
        });
    }

    public static class MSField implements INBTSerializable<NBTTagCompound> {
        public static final int BOMB = -1;
        public static final int EMPTY = 0;
        public static final int NO_MARK = 0;
        public static final int FLAG = 1;
        public static final int QUESTION_MARK = 2;

        private int type;
        private int mark;
        private boolean isHidden;

        public MSField(int type, boolean isHidden, int mark) {
            this.type = type;
            this.isHidden = isHidden;
            this.mark = mark;
        }

        private void swapMark() {
            if (mark == 2) mark = 0;
            else mark += 1;
        }

        private void resetMark() { mark = NO_MARK; }
        private void reveal() { isHidden = false; resetMark(); }
        public int getType() { return type; }
        public int getMark() { return mark; }
        public boolean isHidden() { return isHidden; }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound field = new NBTTagCompound();
            field.setInteger("mark", mark);
            field.setBoolean("hidden", isHidden);
            field.setInteger("type", type);
            return field;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            isHidden = nbt.getBoolean("hidden");
            mark = nbt.getInteger("mark");
            type = nbt.getInteger("type");
        }

        @Override
        public String toString() {
            return "Field{type: " + type + ", hidden: " + isHidden + ", mark: " + mark + "}";
        }
    }
}
