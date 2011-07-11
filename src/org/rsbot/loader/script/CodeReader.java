package org.rsbot.loader.script;

import org.rsbot.loader.asm.Label;
import org.rsbot.loader.asm.MethodVisitor;

/**
 */
public class CodeReader {

	static interface Opcodes {
		int INSN = 1;
		int INT_INSN = 2;
		int VAR_INSN = 3;
		int TYPE_INSN = 4;
		int FIELD_INSN = 5;
		int METHOD_INSN = 6;
		int JUMP_INSN = 7;
		int LDC_INSN = 8;
		int IINC_INSN = 9;
		int TABLESWITCH_INSN = 10;
		int LOOKUPSWITCH_INSN = 11;
		int MULTIANEWARRAY_INSN = 12;
		int TRY_CATCH_BLOCK = 13;
		int LOCAL_VARIABLE = 14;
		int LABEL = 15;
	}

	private final Scanner code;

	public CodeReader(final byte[] code) {
		this.code = new Scanner(code);
	}

	public void accept(final MethodVisitor v) {
		int len = code.readShort();
		final Label[] labels = new Label[code.readByte()];
		for (int i = 0, l = labels.length; i < l; ++i) {
			labels[i] = new Label();
		}
		while (len-- > 0) {
			final int op = code.readByte();
			if (op == Opcodes.INSN) {
				v.visitInsn(code.readByte());
			} else if (op == Opcodes.INT_INSN) {
				v.visitIntInsn(code.readByte(), code.readShort());
			} else if (op == Opcodes.VAR_INSN) {
				v.visitVarInsn(code.readByte(), code.readByte());
			} else if (op == Opcodes.TYPE_INSN) {
				v.visitTypeInsn(code.readByte(), code.readString());
			} else if (op == Opcodes.FIELD_INSN) {
				v.visitFieldInsn(code.readByte(), code.readString(), code.readString(), code.readString());
			} else if (op == Opcodes.METHOD_INSN) {
				v.visitMethodInsn(code.readByte(), code.readString(), code.readString(), code.readString());
			} else if (op == Opcodes.JUMP_INSN) {
				v.visitJumpInsn(code.readByte(), labels[code.readByte()]);
			} else if (op == Opcodes.LDC_INSN) {
				final int type = code.readByte();
				if (type == 1) {
					v.visitLdcInsn(code.readInt());
				} else if (type == 2) {
					v.visitLdcInsn(Float.parseFloat(code.readString()));
				} else if (type == 3) {
					v.visitLdcInsn(code.readLong());
				} else if (type == 4) {
					v.visitLdcInsn(Double.parseDouble(code.readString()));
				} else if (type == 5) {
					v.visitLdcInsn(code.readString());
				}
			} else if (op == Opcodes.IINC_INSN) {
				v.visitIincInsn(code.readByte(), code.readByte());
			} else if (op == Opcodes.TABLESWITCH_INSN) {
				final int min = code.readShort();
				final int max = code.readShort();
				final Label dflt = labels[code.readByte()];
				final int n = code.readByte();
				int ptr = 0;
				final Label[] lbls = new Label[n];
				while (ptr < n) {
					lbls[ptr++] = labels[code.readByte()];
				}
				v.visitTableSwitchInsn(min, max, dflt, lbls);
			} else if (op == Opcodes.LOOKUPSWITCH_INSN) {
				final Label dflt = labels[code.readByte()];
				int n = code.readByte(), ptr = 0;
				final int[] keys = new int[n];
				while (ptr < n) {
					keys[ptr++] = code.readShort();
				}
				n = code.readByte();
				ptr = 0;
				final Label[] lbls = new Label[n];
				while (ptr < n) {
					lbls[ptr++] = labels[code.readByte()];
				}
				v.visitLookupSwitchInsn(dflt, keys, lbls);
			} else if (op == Opcodes.MULTIANEWARRAY_INSN) {
				v.visitMultiANewArrayInsn(code.readString(), code.readByte());
			} else if (op == Opcodes.TRY_CATCH_BLOCK) {
				v.visitTryCatchBlock(labels[code.readByte()], labels[code.readByte()], labels[code.readByte()], code.readString());
			} else if (op == Opcodes.LOCAL_VARIABLE) {
				v.visitLocalVariable(code.readString(), code.readString(), code.readString(), labels[code.readByte()], labels[code.readByte()],
						code.readByte());
			} else if (op == Opcodes.LABEL) {
				v.visitLabel(labels[code.readByte()]);
			}
		}
	}

}
