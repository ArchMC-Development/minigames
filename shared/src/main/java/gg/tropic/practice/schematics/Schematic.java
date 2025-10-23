package gg.tropic.practice.schematics;

import gg.tropic.practice.schematics.jnbt.Tag;
import gg.tropic.practice.schematics.manipulation.BlockOffset;

import java.util.List;

/*
 * @author MineBug.de Development
 *
 * Created by TheHolyException at 25.11.2018 - 01:51:06
 */

public class Schematic {

	private short[] blocks;
	private byte[] data;
	private List<Tag> tileentities;
	private short width;
	private short lenght;
	private short height;
	private BlockOffset offset;

	public Schematic(short[] blocks, byte[] data, List<Tag> tileentities, short width, short lenght, short height, BlockOffset offset) {
		this.blocks = blocks;
		this.data = data;
		this.tileentities = tileentities;
		this.width = width;
		this.lenght = lenght;
		this.height = height;
		this.offset = offset;
	}

	public short[] getBlocks() {
		return blocks;
	}

	public byte[] getData() {
		return data;
	}

	public List<Tag> getTileentities() {
		return tileentities;
	}

	public short getWidth() {
		return width;
	}

	public short getLength() {
		return lenght;
	}

	public short getHeight() {
		return height;
	}

	public BlockOffset getOffset() {
		return offset;
	}
}
