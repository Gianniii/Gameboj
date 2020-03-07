package ch.epfl.gameboj.component.cpu;

import static ch.epfl.gameboj.Preconditions.checkBits8;
import static ch.epfl.gameboj.Preconditions.checkBits16;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Alu.RotDir;
import ch.epfl.gameboj.component.cpu.Opcode.Kind;
import ch.epfl.gameboj.component.memory.Ram;

/**
 * Class CPU, processor of the GameBoy
 * 
 * @author Omid Karimi (273816)
 * @author Gianni Lodetti (275085)
 */
public final class Cpu implements Component, Clocked {

    private long nextNonIdleCycle = 0;
    private static final Opcode[] DIRECT_OPCODE_TABLE = buildOpcodeTable(Opcode.Kind.DIRECT);
    private static final Opcode[] PREFIXED_OPCODE_TABLE = buildOpcodeTable(Opcode.Kind.PREFIXED);
    private static final int OPCODE_PREFIX = 0xCB;
    private final RegisterFile<Reg> bench = new RegisterFile<>(Reg.values());
    private int Pc = 0;
    private int Sp = 0;
    private Bus bus;
    private Ram highRam = new Ram(AddressMap.HIGH_RAM_SIZE);
    private boolean IME = false;
    private int regIE = 0;
    private int regIF = 0;

    private enum Reg implements Register {
        A, F, B, C, D, E, H, L
    }

    private enum Reg16 implements Register {
        AF, BC, DE, HL
    }

    private enum FlagSrc {
        V0, V1, ALU, CPU
    }

    /**
     * types of interruption that could be requested
     *
     */
    public enum Interrupt implements Bit {
        VBLANK, LCD_STAT, TIMER, SERIAL, JOYPAD
    }

    @Override
    public int read(int address) throws IllegalArgumentException {
        address = checkBits16(address);
        if (address < AddressMap.HIGH_RAM_END && address >= AddressMap.HIGH_RAM_START) {
            return highRam.read(address - AddressMap.HIGH_RAM_START);
        }
        if (address == AddressMap.REG_IE) {
            return regIE;
        }
        if (address == AddressMap.REG_IF) {
            return regIF;
        }
        return Component.NO_DATA;
    }

    @Override
    public void write(int address, int data) throws IllegalArgumentException {
        address = checkBits16(address);
        data = checkBits8(data);
        if (address < AddressMap.HIGH_RAM_END && address >= AddressMap.HIGH_RAM_START) {
            highRam.write(address - AddressMap.HIGH_RAM_START, data);
        }
        if (address == AddressMap.REG_IE) {
            regIE = data;
        }
        if (address == AddressMap.REG_IF) {
            regIF = data;
        }
    }

    @Override
    public void cycle(long cycle) {
        //wakes the cpu when prompted to
        if (nextNonIdleCycle == Long.MAX_VALUE && (regIE & regIF) != 0) {
            nextNonIdleCycle = cycle;
        }
        if (nextNonIdleCycle == cycle && nextNonIdleCycle != Long.MAX_VALUE) {
            reallyCycle();
        }
    }

    /**
     * called by the method cycle() when the cpu is not on halt
     */
    private void reallyCycle() {
        // interruption manager
        if (IME && (regIE & regIF) != 0) {
            IME = false;
            int index = 31 - Integer.numberOfLeadingZeros(Integer.lowestOneBit(regIF & regIE));
            regIF = Bits.set(regIF, index, false);
            push16(Pc);
            Pc = AddressMap.INTERRUPTS[index];
            nextNonIdleCycle += 5;
        } else {
            Opcode opcode = read8(Pc) == OPCODE_PREFIX ? 
                    PREFIXED_OPCODE_TABLE[read8(Pc + 1)] : DIRECT_OPCODE_TABLE[read8(Pc)];
            dispatch(opcode);
            nextNonIdleCycle += opcode.cycles;
            if (testCondition(opcode)) 
                nextNonIdleCycle += opcode.additionalCycles;
        }
    }

    @Override
    public void attachTo(Bus bus) {
        this.bus = bus;
        Component.super.attachTo(bus);
    }

    /**
     * facilitates the tests of the instructions
     * 
     * @return array with all the registers values
     */
    public int[] _testGetPcSpAFBCDEHL() {
        int[] PcSpAFBCDEHL = new int[] { Pc, Sp, bench.get(Reg.A),
                bench.get(Reg.F), bench.get(Reg.B), bench.get(Reg.C),
                bench.get(Reg.D), bench.get(Reg.E), bench.get(Reg.H),
                bench.get(Reg.L) };
        return PcSpAFBCDEHL;
    }

    /**
     * request an interruption
     * 
     * @param interruption
     */
    public void requestInterrupt(Interrupt i) {
        regIF = regIF | (1 << i.index());
    }

    // treats the instructions
    private void dispatch(Opcode opcode) {
        int nextPc = Pc + opcode.totalBytes;
        switch (opcode.family) {
        case NOP: {
        }
            break;
        case LD_R8_HLR: {
            bench.set(extractReg(opcode, 3), read8AtHl());
        }
            break;
        case LD_A_HLRU: {
            bench.set(Reg.A, read8AtHl());
            setReg16(Reg16.HL, Bits.clip(16, reg16(Reg16.HL) + extractHlIncrement(opcode)));
        }
            break;
        case LD_A_N8R: {
            bench.set(Reg.A, read8(AddressMap.REGS_START + read8AfterOpcode()));
        }
            break;
        case LD_A_CR: {
            bench.set(Reg.A, read8(AddressMap.REGS_START + bench.get(Reg.C)));
        }
            break;
        case LD_A_N16R: {
            bench.set(Reg.A, read8(read16AfterOpcode()));
        }
            break;
        case LD_A_BCR: {
            bench.set(Reg.A, read8(reg16(Reg16.BC)));
        }
            break;
        case LD_A_DER: {
            bench.set(Reg.A, read8(reg16(Reg16.DE)));
        }
            break;
        case LD_R8_N8: {
            bench.set(extractReg(opcode, 3), read8AfterOpcode());
        }
            break;
        case LD_R16SP_N16: {
            setReg16SP(extractReg16(opcode), read16AfterOpcode());
        }
            break;
        case POP_R16: {
            setReg16(extractReg16(opcode), pop16());
        }
            break;
        case LD_HLR_R8: {
            write8AtHl(bench.get(extractReg(opcode, 0)));
        }
            break;
        case LD_HLRU_A: {
            write8AtHl(bench.get(Reg.A));
            setReg16(Reg16.HL, Bits.clip(16, reg16(Reg16.HL) + extractHlIncrement(opcode)));
        }
            break;
        case LD_N8R_A: {
            write8(AddressMap.REGS_START + read8AfterOpcode(), bench.get(Reg.A));
        }
            break;
        case LD_CR_A: {
            write8(AddressMap.REGS_START + bench.get(Reg.C), bench.get(Reg.A));
        }
            break;
        case LD_N16R_A: {
            write8(read16AfterOpcode(), bench.get(Reg.A));
        }
            break;
        case LD_BCR_A: {
            write8(reg16(Reg16.BC), bench.get(Reg.A));
        }
            break;
        case LD_DER_A: {
            write8(reg16(Reg16.DE), bench.get(Reg.A));
        }
            break;
        case LD_HLR_N8: {
            write8(reg16(Reg16.HL), read8AfterOpcode());
        }
            break;
        case LD_N16R_SP: {
            write16(read16AfterOpcode(), Sp);
        }
            break;
        case LD_R8_R8: {
            Reg r = extractReg(opcode, 3);
            Reg s = extractReg(opcode, 0);
            bench.set(r, bench.get(s));
        }
            break;
        case LD_SP_HL: {
            Sp = reg16(Reg16.HL);
        }
            break;
        case PUSH_R16: {
            push16(reg16(extractReg16(opcode)));
        }
            break;
            
        // Add
        case ADD_A_N8: {
            int newValueA = Alu.add(bench.get(Reg.A), read8AfterOpcode(),
                    extractFlag(Alu.Flag.C) && Bits.test(opcode.encoding, 3));
            setRegFlags(Reg.A, newValueA);
        }
            break;
        case ADD_A_R8: {
            int newValueA = Alu.add(bench.get(Reg.A), bench.get(extractReg(opcode, 0)),
                        extractFlag(Alu.Flag.C) && Bits.test(opcode.encoding, 3));
            setRegFlags(Reg.A, newValueA);
        }
            break;
        case ADD_A_HLR: {
            int newValueA = Alu.add(bench.get(Reg.A), read8AtHl(),
                    extractFlag(Alu.Flag.C) && Bits.test(opcode.encoding, 3));
            setRegFlags(Reg.A, newValueA);
        }
            break;
        case INC_R8: {
            int newValue = Alu.add(bench.get(extractReg(opcode, 3)), 1);
            setRegFromAlu(extractReg(opcode, 3), newValue);
            combineAluFlags(newValue, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.CPU);
        }
            break;
        case INC_HLR: {
            int newValue = Alu.add(read8AtHl(), 1);
            write8AtHl(Alu.unpackValue(newValue));
            combineAluFlags(newValue, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.CPU);
        }
            break;
        case INC_R16SP: {
            Reg16 r = extractReg16(opcode);
            if (r == Reg16.AF) {
                Sp = Alu.unpackValue(Alu.add16H(Sp, 1));
            } else {
                setReg16(r, Alu.unpackValue(Alu.add16H(reg16(r), 1)));
            }
        }
            break;
        case ADD_HL_R16SP: {
            Reg16 r = extractReg16(opcode);
            int registerValue = r == Reg16.AF ? Sp : reg16(r);
            int newValue = Alu.add16H(registerValue, reg16(Reg16.HL));
            setReg16(Reg16.HL, Alu.unpackValue(newValue));
            combineAluFlags(newValue, FlagSrc.CPU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
        }
            break;
        case LD_HLSP_S8: {
            int newValue = Alu.add16L(Sp, Bits.clip(16, signExtendOpcode(opcode)));
            if (Bits.test(opcode.encoding, 4)) {
                setReg16(Reg16.HL, Alu.unpackValue(newValue));
            } else {
                Sp = Alu.unpackValue(newValue);
            }
            combineAluFlags(newValue, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
        }
            break;

        // Subtract
        case SUB_A_N8: {
            int newValueA = Alu.sub(bench.get(Reg.A), read8AfterOpcode(),
                        extractFlag(Alu.Flag.C) && Bits.test(opcode.encoding, 3));
            setRegFlags(Reg.A, newValueA);
        }
            break;
        case SUB_A_R8: {
            int newValueA = Alu.sub(bench.get(Reg.A), bench.get(extractReg(opcode, 0)),
                        extractFlag(Alu.Flag.C) && Bits.test(opcode.encoding, 3));
            setRegFlags(Reg.A, newValueA);
        }
            break;
        case SUB_A_HLR: {
            int newValueA = Alu.sub(bench.get(Reg.A), read8AtHl(),
                        extractFlag(Alu.Flag.C) && Bits.test(opcode.encoding, 3));
            setRegFlags(Reg.A, newValueA);
        }
            break;
        case DEC_R8: {
            int newValue = Alu.sub(bench.get(extractReg(opcode, 3)), 1);
            setRegFromAlu(extractReg(opcode, 3), newValue);
            combineAluFlags(newValue, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.CPU);
        }
            break;
        case DEC_HLR: {
            int newValue = Alu.sub(read8AtHl(), 1);
            write8AtHl(Alu.unpackValue(newValue));
            combineAluFlags(newValue, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.CPU);
        }
            break;
        case CP_A_R8: {
            int newValueA = Alu.sub(bench.get(Reg.A), bench.get(extractReg(opcode, 0)));
            setFlags(newValueA);
        }
            break;
        case CP_A_N8: {
            int newValueA = Alu.sub(bench.get(Reg.A), read8AfterOpcode());
            setFlags(newValueA);
        }
            break;
        case CP_A_HLR: {
            int newValueA = Alu.sub(bench.get(Reg.A), read8AtHl());
            setFlags(newValueA);
        }
            break;
        case DEC_R16SP: {
            Reg16 r = extractReg16(opcode);
            if (r == Reg16.AF) {
                Sp = Bits.clip(16, Sp - 1);
            } else {
                setReg16(r, Bits.clip(16, reg16(r) - 1));
            }
        }
            break;

        // And, or, xor, complement
        case AND_A_N8: {
            int newValue = Alu.and(bench.get(Reg.A), read8AfterOpcode());
            setRegFlags(Reg.A, newValue);
        }
            break;
        case AND_A_R8: {
            int newValue = Alu.and(bench.get(Reg.A), bench.get(extractReg(opcode, 0)));
            setRegFlags(Reg.A, newValue);
        }
            break;
        case AND_A_HLR: {
            int newValue = Alu.and(bench.get(Reg.A), read8AtHl());
            setRegFlags(Reg.A, newValue);
        }
            break;
        case OR_A_R8: {
            int newValue = Alu.or(bench.get(Reg.A), bench.get(extractReg(opcode, 0)));
            setRegFlags(Reg.A, newValue);
        }
            break;
        case OR_A_N8: {
            int newValue = Alu.or(bench.get(Reg.A), read8AfterOpcode());
            setRegFlags(Reg.A, newValue);
        }
            break;
        case OR_A_HLR: {
            int newValue = Alu.or(bench.get(Reg.A), read8AtHl());
            setRegFlags(Reg.A, newValue);
        }
            break;
        case XOR_A_R8: {
            int newValue = Alu.xor(bench.get(Reg.A), bench.get(extractReg(opcode, 0)));
            setRegFlags(Reg.A, newValue);
        }
            break;
        case XOR_A_N8: {
            int newValue = Alu.xor(bench.get(Reg.A), read8AfterOpcode());
            setRegFlags(Reg.A, newValue);
        }
            break;
        case XOR_A_HLR: {
            int newValue = Alu.xor(bench.get(Reg.A), read8AtHl());
            setRegFlags(Reg.A, newValue);
        }
            break;
        case CPL: {
            int newValue = Bits.complement8(bench.get(Reg.A));
            bench.set(Reg.A, newValue);
            combineAluFlags(newValue, FlagSrc.CPU, FlagSrc.V1, FlagSrc.V1, FlagSrc.CPU);
        }
            break;

        // Rotate, shift
        case ROTCA: {
            RotDir rotDir = getRotDir(opcode);
            int newValue = Alu.rotate(rotDir, bench.get(Reg.A));
            setRegFromAlu(Reg.A, newValue);
            combineAluFlags(newValue, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
        }
            break;
        case ROTA: {
            RotDir rotDir = getRotDir(opcode);
            int newValue = Alu.rotate(rotDir, bench.get(Reg.A), extractFlag(Alu.Flag.C));
            setRegFromAlu(Reg.A, newValue);
            combineAluFlags(newValue, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
        }
            break;
        case ROTC_R8: {
            Reg r = extractReg(opcode, 0);
            RotDir rotDir = getRotDir(opcode);
            int newValue = Alu.rotate(rotDir, bench.get(r));
            setRegFlags(r, newValue);
        }
            break;
        case ROT_R8: {
            Reg r = extractReg(opcode, 0);
            RotDir rotDir = getRotDir(opcode);
            int newValue = Alu.rotate(rotDir, bench.get(r), extractFlag(Alu.Flag.C));
            setRegFlags(r, newValue);
        }
            break;
        case ROTC_HLR: {
            RotDir rotDir = getRotDir(opcode);
            int newValue = Alu.rotate(rotDir, read8AtHl());
            write8AtHlAndSetFlags(newValue);
        }
            break;
        case ROT_HLR: {
            RotDir rotDir = getRotDir(opcode);
            int newValue = Alu.rotate(rotDir, read8AtHl(), extractFlag(Alu.Flag.C));
            write8AtHlAndSetFlags(newValue);
        }
            break;
        case SWAP_R8: {
            int newValue = Alu.swap(bench.get(extractReg(opcode, 0)));
            setRegFlags(extractReg(opcode, 0), newValue);
        }
            break;
        case SWAP_HLR: {
            int newValue = Alu.swap(read8AtHl());
            write8AtHlAndSetFlags(newValue);
        }
            break;
        case SLA_R8: {
            int newValue = Alu.shiftLeft(bench.get(extractReg(opcode, 0)));
            setRegFlags(extractReg(opcode, 0), newValue);
        }
            break;
        case SRA_R8: {
            int newValue = Alu.shiftRightA(bench.get(extractReg(opcode, 0)));
            setRegFlags(extractReg(opcode, 0), newValue);
        }
            break;
        case SRL_R8: {
            int newValue = Alu.shiftRightL(bench.get(extractReg(opcode, 0)));
            setRegFlags(extractReg(opcode, 0), newValue);
        }
            break;
        case SLA_HLR: {
            int newValue = Alu.shiftLeft(read8AtHl());
            write8AtHlAndSetFlags(newValue);
        }
            break;
        case SRA_HLR: {
            int newValue = Alu.shiftRightA(read8AtHl());
            write8AtHlAndSetFlags(newValue);
        }
            break;
        case SRL_HLR: {
            int newValue = Alu.shiftRightL(read8AtHl());
            write8AtHlAndSetFlags(newValue);
        }
            break;

        // Bit test and set
        case BIT_U3_R8: {
            Reg r = extractReg(opcode, 0);
            int newValue = Alu.testBit(bench.get(r), extractIndex(opcode));
            combineAluFlags(newValue, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V1, FlagSrc.CPU);
        }
            break;
        case BIT_U3_HLR: {
            int newValue = Alu.testBit(read8AtHl(), extractIndex(opcode));
            combineAluFlags(newValue, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V1, FlagSrc.CPU);
        }
            break;
        case CHG_U3_R8: {
            Reg r = extractReg(opcode, 0);
            int index = extractIndex(opcode);
            if (Bits.test(opcode.encoding, 6)) {
                setRegFromAlu(r, Alu.or(bench.get(r), 1 << index));
            } else {
                setRegFromAlu(r, Alu.and(bench.get(r), Bits.complement8(1 << index)));
            }
        }
            break;
        case CHG_U3_HLR: {
            int index = extractIndex(opcode);
            if (Bits.test(opcode.encoding, 6)) {
                write8AtHl(Alu.unpackValue(Alu.or(read8AtHl(), 1 << index)));
            } else {
                write8AtHl(Alu.unpackValue(Alu.and(read8AtHl(), Bits.complement8(1 << index))));
            }
        }
            break;

        // Misc. ALU
        case DAA: {
            int newValue = Alu.bcdAdjust(bench.get(Reg.A), extractFlag(Alu.Flag.N), 
                                         extractFlag(Alu.Flag.H), extractFlag(Alu.Flag.C));
            setRegFromAlu(Reg.A, newValue);
            combineAluFlags(newValue, FlagSrc.ALU, FlagSrc.CPU, FlagSrc.V0, FlagSrc.ALU);
        }
            break;
        case SCCF: {
            int newFlags = bench.get(Reg.F) ^ Bits.mask(Alu.Flag.C.index());
            FlagSrc flagSrc = Bits.test(opcode.encoding, 3) ? FlagSrc.ALU : FlagSrc.V1;
            combineAluFlags(newFlags, FlagSrc.CPU, FlagSrc.V0, FlagSrc.V0, flagSrc);
        }
            break;

        // Jumps
        case JP_HL: {
            nextPc = reg16(Reg16.HL);
        }
            break;
        case JP_N16: {
            nextPc = read16AfterOpcode();
        }
            break;
        case JP_CC_N16: {
            if (testCondition(opcode))
                nextPc = read16AfterOpcode();
        }
            break;
        case JR_E8: {
            nextPc = Bits.clip(16, nextPc + signExtendOpcode(opcode));
        }
            break;
        case JR_CC_E8: {
            if (testCondition(opcode))
                nextPc = Bits.clip(16, nextPc + signExtendOpcode(opcode));
        }
            break;

        // Calls and returns
        case CALL_N16: {
            push16(nextPc);
            nextPc = read16AfterOpcode();
        }
            break;
        case CALL_CC_N16: {
            if (testCondition(opcode)) {
                push16(nextPc);
                nextPc = read16AfterOpcode();
            }
        }
            break;
        case RST_U3: {
            push16(nextPc);
            nextPc = AddressMap.RESETS[extractIndex(opcode)];
        }
            break;
        case RET: {
            nextPc = pop16();
        }
            break;
        case RET_CC: {
            if (testCondition(opcode)) 
                nextPc = pop16();
        }
            break;

        // Interrupts
        case EDI: {
            IME = Bits.test(opcode.encoding, 3);
        }
            break;
        case RETI: {
            IME = true;
            nextPc = pop16();
        }
            break;

        // Misc control
        case HALT: {
            nextNonIdleCycle = Long.MAX_VALUE;
        }
            break;
        case STOP:
            throw new Error("STOP is not implemented");
        }
        Pc = nextPc;
    }
    
    private RotDir getRotDir(Opcode opcode) {
        return Bits.test(opcode.encoding, 3) ? RotDir.RIGHT : RotDir.LEFT;
    }
    
    private int signExtendOpcode(Opcode opcode) {
        return Bits.signExtend8(read8AfterOpcode());
    }

    // creates a table with opcodes encoding
    private static Opcode[] buildOpcodeTable(Kind kind) {
        Opcode[] opcodeTable = new Opcode[256];
        for (Opcode o : Opcode.values()) {
            if (o.kind == kind) {
                opcodeTable[o.encoding] = o;
            }
        }
        return opcodeTable;
    }

    // read and write methods for the instructions
    private int read8(int address) {
        return bus.read(address);
    }

    private int read8AtHl() {
        return read8(reg16(Reg16.HL));
    }

    private int read8AfterOpcode() {
        return read8(Pc + 1);
    }

    private int read16(int address) {
        return Bits.make16(read8(address + 1), read8(address));
    }

    private int read16AfterOpcode() {
        return read16(Pc + 1);
    }

    private void write8(int address, int v) {
        bus.write(address, v);
    }

    private void write16(int address, int v) {
        write8(address, Bits.clip(8, v));
        write8(Bits.clip(16, address + 1), Bits.extract(v, 8, 8));
    }

    private void write8AtHl(int v) {
        write8(reg16(Reg16.HL), v);
    }

    private void push16(int v) {
        Sp = Bits.clip(16, Sp - 2);
        write16(Sp, v);
    }

    private int pop16() {
        int value = read16(Sp);
        Sp = Bits.clip(16, Sp + 2);
        return value;
    }

    // methods to manipulate 16 bits registers
    private int reg16(Reg16 r) {
        switch (r) {
        case AF: {
            return Bits.make16(bench.get(Reg.A), bench.get(Reg.F));
        }
        case BC: {
            return Bits.make16(bench.get(Reg.B), bench.get(Reg.C));
        }
        case DE: {
            return Bits.make16(bench.get(Reg.D), bench.get(Reg.E));
        }
        case HL: {
            return Bits.make16(bench.get(Reg.H), bench.get(Reg.L));
        }
        default: {
            throw new IllegalArgumentException("Non valid register");
        }
        }
    }

    private void setReg16(Reg16 r, int newV) {
        newV = checkBits16(newV);
        switch (r) {
        case AF: {
            bench.set(Reg.A, Bits.extract(newV, 8, 8));
            // to make sure that 4 lsb bits are 0
            bench.set(Reg.F, Bits.clip(8, newV) & (-1 << 4));
        }
            break;
        case BC: {
            bench.set(Reg.B, Bits.extract(newV, 8, 8));
            bench.set(Reg.C, Bits.clip(8, newV));
        }
            break;
        case DE: {
            bench.set(Reg.D, Bits.extract(newV, 8, 8));
            bench.set(Reg.E, Bits.clip(8, newV));
        }
            break;
        case HL: {
            bench.set(Reg.H, Bits.extract(newV, 8, 8));
            bench.set(Reg.L, Bits.clip(8, newV));
        }
            break;
        }
    }

    private void setReg16SP(Reg16 r, int newV) {
        newV = checkBits16(newV);
        if (r == Reg16.AF) {
            Sp = newV;
        } else {
            setReg16(r, newV);
        }
    }

    // methods to extract the register from opcode
    private Reg extractReg(Opcode opcode, int startBit) {
        int register = Bits.extract(opcode.encoding, startBit, 3);
        if (register == 0b111) {
            return Reg.A;
        } else if (register == 0b110) {
            throw new NullPointerException("Non valid register");
        }
        return Reg.values()[register + 2];
    }

    private Reg16 extractReg16(Opcode opcode) {
        int register = Bits.extract(opcode.encoding, 4, 2);
        if (register == 0b11) {
            return Reg16.AF;
        }
        return Reg16.values()[register + 1];
    }

    private int extractHlIncrement(Opcode opcode) {
        return Bits.test(opcode.encoding, 4) ? -1 : 1;
    }

    // methods to manipulate flags
    private void setRegFromAlu(Reg r, int vf) {
        bench.set(r, Alu.unpackValue(vf));
    }

    private void setFlags(int valueFlags) {
        bench.set(Reg.F, Alu.unpackFlags(valueFlags));
    }

    private void setRegFlags(Reg r, int vf) {
        setRegFromAlu(r, vf);
        setFlags(vf);
    }

    private void write8AtHlAndSetFlags(int vf) {
        write8AtHl(Alu.unpackValue(vf));
        setFlags(vf);
    }

    private void combineAluFlags(int vf, FlagSrc z, FlagSrc n, FlagSrc h, FlagSrc c) {
        int cpuFlags = bench.get(Reg.F);
        int aluFlags = Alu.unpackFlags(vf);
        int maskV1 = Alu.maskZNHC(z == FlagSrc.V1, n == FlagSrc.V1,
                                  h == FlagSrc.V1, c == FlagSrc.V1);
        int maskCPU = Alu.maskZNHC(z == FlagSrc.CPU, n == FlagSrc.CPU,
                                   h == FlagSrc.CPU, c == FlagSrc.CPU);
        int maskALU = Alu.maskZNHC(z == FlagSrc.ALU, n == FlagSrc.ALU,
                                   h == FlagSrc.ALU, c == FlagSrc.ALU);
        int newFlags = (cpuFlags & maskCPU) | (aluFlags & maskALU) | maskV1;
        setFlags(newFlags);
    }

    // extract the index determined by opcode (for some instruction)
    private int extractIndex(Opcode opcode) {
        return Bits.extract(opcode.encoding, 3, 3);
    }

    // get the value of the given flag that is in register F
    private boolean extractFlag(Alu.Flag flag) {
        return Bits.test(bench.get(Reg.F), flag.index());
    }

    // for instruction with condition
    private boolean testCondition(Opcode opcode) {
        int cc = Bits.extract(opcode.encoding, 3, 2);
        if (cc == 0b00)
            return !extractFlag(Alu.Flag.Z);
        if (cc == 0b01)
            return extractFlag(Alu.Flag.Z);
        if (cc == 0b10)
            return !extractFlag(Alu.Flag.C);
        if (cc == 0b11)
            return extractFlag(Alu.Flag.C);
        // it should never happen
        return false;
    }
}
