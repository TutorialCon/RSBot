package org.rsbot.loader;

import org.rsbot.loader.asm.*;

public class VersionVisitor implements ClassVisitor {
	private int version;

	public int getVersion() {
		return version;
	}

	public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
	}

	public void visitSource(final String source, final String debug) {
	}

	public void visitOuterClass(final String owner, final String name, final String desc) {
	}

	public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
		return null;
	}

	public void visitAttribute(final Attribute attr) {
	}

	public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {
	}

	public FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value) {
		return null;
	}

	public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
		if (!name.equals("main")) {
			return null;
		}
		return new MethodVisitor() {
			public AnnotationVisitor visitAnnotationDefault() {
				return null;
			}

			public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
				return null;
			}

			public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
				return null;
			}

			public void visitAttribute(final Attribute attr) {
			}

			public void visitCode() {
			}

			public void visitFrame(final int type, final int nLocal, final Object[] local, final int nStack, final Object[] stack) {
			}

			public void visitInsn(final int opcode) {
			}

			public void visitIntInsn(final int opcode, final int operand) {
				if (opcode == Opcodes.SIPUSH && operand > 400 && operand < 768) {
					version = operand;
				}
			}

			public void visitVarInsn(final int opcode, final int var) {
			}

			public void visitTypeInsn(final int opcode, final String type) {
			}

			public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
			}

			public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
			}

			public void visitJumpInsn(final int opcode, final Label label) {
			}

			public void visitLabel(final Label label) {
			}

			public void visitLdcInsn(final Object cst) {
			}

			public void visitIincInsn(final int var, final int increment) {
			}

			public void visitTableSwitchInsn(final int min, final int max, final Label dflt, final Label[] labels) {
			}

			public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
			}

			public void visitMultiANewArrayInsn(final String desc, final int dims) {
			}

			public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
			}

			public void visitLocalVariable(final String name, final String desc, final String signature, final Label start, final Label end, final int index) {
			}

			public void visitLineNumber(final int line, final Label start) {
			}

			public void visitMaxs(final int maxStack, final int maxLocals) {
			}

			public void visitEnd() {
			}
		};
	}

	public void visitEnd() {
	}
}