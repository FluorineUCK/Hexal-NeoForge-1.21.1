package ram.talia.hexal.api.casting.iota;

import at.petrak.hexcasting.api.casting.iota.Iota;

import java.util.function.UnaryOperator;

/** Allows compound addon iotas to participate in Hexal's inter-world sanitisation. */
public interface IMappableIota {
    Iota mapSubIotas(UnaryOperator<Iota> mapper);
}
