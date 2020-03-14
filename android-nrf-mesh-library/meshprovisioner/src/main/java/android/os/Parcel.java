package android.os;

import android.annotation.UnsupportedAppUsage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Parcel {
    private int dataCapacity = 0;
    private int dataPosition = 0;


    /**
     * Write a byte array into the parcel at the current {@link #dataPosition},
     * growing {@link #dataCapacity} if needed.
     * @param b Bytes to place into the parcel.
     */
    public final void writeByteArray(@Nullable byte[] b) {
        writeByteArray(b, 0, (b != null) ? b.length : 0);
    }

    /**
     * Write a byte array into the parcel at the current {@link #dataPosition},
     * growing {@link #dataCapacity} if needed.
     * @param b Bytes to place into the parcel.
     * @param offset Index of first byte to be written.
     * @param len Number of bytes to write.
     */
    public final void writeByteArray(@Nullable byte[] b, int offset, int len) {
        if (b == null) {
            writeInt(-1);
            return;
        }

        throw new RuntimeException("Should imp with J2OBJC");
    }

    /**
     * Write a blob of data into the parcel at the current {@link #dataPosition},
     * growing {@link #dataCapacity} if needed.
     * @param b Bytes to place into the parcel.
     * {@hide}
     * {@SystemApi}
     */
    @UnsupportedAppUsage
    public final void writeBlob(@Nullable byte[] b) {
        writeBlob(b, 0, (b != null) ? b.length : 0);
    }

    /**
     * Write a blob of data into the parcel at the current {@link #dataPosition},
     * growing {@link #dataCapacity} if needed.
     * @param b Bytes to place into the parcel.
     * @param offset Index of first byte to be written.
     * @param len Number of bytes to write.
     * {@hide}
     * {@SystemApi}
     */
    public final void writeBlob(@Nullable byte[] b, int offset, int len) {
        if (b == null) {
            writeInt(-1);
            return;
        }

        throw new RuntimeException("Should imp with J2OBJC");
    }

    /**
     * Write an integer value into the parcel at the current dataPosition(),
     * growing dataCapacity() if needed.
     */
    public final void writeInt(int val) {
        throw new RuntimeException("Should imp with J2OBJC");
    }

    /**
     * Write a long integer value into the parcel at the current dataPosition(),
     * growing dataCapacity() if needed.
     */
    public final void writeLong(long val) {
        throw new RuntimeException("Should imp with J2OBJC");
    }

    /**
     * Write a floating point value into the parcel at the current
     * dataPosition(), growing dataCapacity() if needed.
     */
    public final void writeFloat(float val) {
        throw new RuntimeException("Should imp with J2OBJC");
    }

    /**
     * Write a double precision floating point value into the parcel at the
     * current dataPosition(), growing dataCapacity() if needed.
     */
    public final void writeDouble(double val) {
        throw new RuntimeException("Should imp with J2OBJC");
    }

    /**
     * Write a string value into the parcel at the current dataPosition(),
     * growing dataCapacity() if needed.
     */
    public final void writeString(@Nullable String val) {
        throw new RuntimeException("Should imp with J2OBJC");
    }

    /**
     * Write a boolean value into the parcel at the current dataPosition(),
     * growing dataCapacity() if needed.
     *
     * <p>Note: This method currently delegates to writeInt with a value of 1 or 0
     * for true or false, respectively, but may change in the future.
     */
    public final void writeBoolean(boolean val) {
        writeInt(val ? 1 : 0);
    }


    /**
     * Write a byte value into the parcel at the current dataPosition(),
     * growing dataCapacity() if needed.
     *
     * <p>Note: This method currently delegates to writeInt but may change in
     * the future.
     */
    public final void writeByte(byte val) {
        writeInt(val);
    }

    /**
     * Flatten a List into the parcel at the current dataPosition(), growing
     * dataCapacity() if needed.  The List values are written using
     */
    public final void writeList(@Nullable List val) {
        if (val == null) {
            writeInt(-1);
            return;
        }
        int N = val.size();
        int i=0;
        writeInt(N);
        while (i < N) {
            // writeValue(val.get(i));
            // i++;
            throw new RuntimeException("Should implement with J2OBJC.");
        }
    }


    /**
     * Read and return a byte[] object from the parcel.
     */
    @Nullable
    public final byte[] createByteArray() {
        throw new RuntimeException("Should imp with J2OBJC");
    }


    /**
     * Read a byte[] object from the parcel and copy it into the
     * given byte array.
     */
    public final void readByteArray(@NonNull byte[] val) {
        throw new RuntimeException("Should imp with J2OBJC");
    }

    /**
     * Read and return a String[] object from the parcel.
     * {@hide}
     */
    @UnsupportedAppUsage
    @Nullable
    public final String[] readStringArray() {
        String[] array = null;

        int length = readInt();
        if (length >= 0)
        {
            array = new String[length];

            for (int i = 0 ; i < length ; i++)
            {
                array[i] = readString();
            }
        }

        return array;
    }

    /**
     * Read and return a new ArrayList object from the parcel at the current
     * dataPosition().  Returns null if the previously written list object was
     * null.  The given class loader will be used to load any enclosed
     * Parcelables.
     */
    @Nullable
    public final ArrayList readArrayList(@Nullable ClassLoader loader) {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        ArrayList l = new ArrayList(N);
        throw new RuntimeException("Should imp with J2OBJC");
    }

    /**
     * Read and return a new Object array from the parcel at the current
     * dataPosition().  Returns null if the previously written array was
     * null.  The given class loader will be used to load any enclosed
     * Parcelables.
     */
    @Nullable
    public final Object[] readArray(@Nullable ClassLoader loader) {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        Object[] l = new Object[N];
        throw new RuntimeException("Should imp with J2OBJC");
    }


    /**
     * Read an integer value from the parcel at the current dataPosition().
     */
    public final int readInt() {
        throw new RuntimeException("Should imp with J2OBJC");
    }

    /**
     * Read a long integer value from the parcel at the current dataPosition().
     */
    public final long readLong() {
        throw new RuntimeException("Should imp with J2OBJC");
    }

    /**
     * Read a floating point value from the parcel at the current
     * dataPosition().
     */
    public final float readFloat() {
        throw new RuntimeException("Should imp with J2OBJC");
    }

    /**
     * Read a double precision floating point value from the parcel at the
     * current dataPosition().
     */
    public final double readDouble() {
        throw new RuntimeException("Should imp with J2OBJC");
    }

    /**
     * Read a string value from the parcel at the current dataPosition().
     */
    @Nullable
    public final String readString() {
        throw new RuntimeException("Should imp with J2OBJC");
    }

    /**
     * Read a boolean value from the parcel at the current dataPosition().
     */
    public final boolean readBoolean() {
        return readInt() != 0;
    }

    /**
     * Read a byte value from the parcel at the current dataPosition().
     */
    public final byte readByte() {
        return (byte)(readInt() & 0xff);
    }

    /**
     * Read into an existing List object from the parcel at the current
     * dataPosition(), using the given class loader to load any enclosed
     * Parcelables.  If it is null, the default class loader is used.
     */
    public final void readList(@NonNull List outVal, @Nullable ClassLoader loader) {
        int N = readInt();
        throw new RuntimeException("Should implement with J2OBJC.");
    }

    /**
     * Read a typed object from a parcel.  The given class loader will be
     * used to load any enclosed Parcelables.  If it is null, the default class
     * loader will be used.
     */
    @Nullable
    public final Object readValue(@Nullable ClassLoader loader) {
        throw new RuntimeException("Should implement with J2OBJC.");
    }

    public final void writeValue(@Nullable Object v) {
        throw new RuntimeException("Should implement with J2OBJC.");
    }


    /**
     * Read and return a new Parcelable from the parcel.  The given class loader
     * will be used to load any enclosed Parcelables.  If it is null, the default
     * class loader will be used.
     * @param loader A ClassLoader from which to instantiate the Parcelable
     * object, or null for the default class loader.
     * @return Returns the newly created Parcelable, or null if a null
     * object has been written.
     * @throws BadParcelableException Throws BadParcelableException if there
     * was an error trying to instantiate the Parcelable.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public final <T extends Parcelable> T readParcelable(@Nullable ClassLoader loader) {
        throw new RuntimeException("Should implement with J2OBJC.");
    }


    /**
     * Flatten the name of the class of the Parcelable and its contents
     * into the parcel.
     *
     * @param p The Parcelable object to be written.
     * @param parcelableFlags Contextual flags as per
     * {@link Parcelable#writeToParcel(Parcel, int) Parcelable.writeToParcel()}.
     */
    public final void writeParcelable(@Nullable Parcelable p, int parcelableFlags) {
        throw new RuntimeException("Should implement with J2OBJC.");
    }


    public final void readIntArray(@NonNull int[] val) {
        int N = readInt();
        if (N == val.length) {
            for (int i=0; i<N; i++) {
                val[i] = readInt();
            }
        } else {
            throw new RuntimeException("bad array lengths");
        }
    }

    public final void writeIntArray(@Nullable int[] val) {
        if (val != null) {
            int N = val.length;
            writeInt(N);
            for (int i=0; i<N; i++) {
                writeInt(val[i]);
            }
        } else {
            writeInt(-1);
        }
    }

    public final <T extends Parcelable> void writeTypedList(@Nullable List<T> val) {
        throw new RuntimeException("Should implement with J2OBJC.");
    }


    /**
     * Read into the given List items containing a particular object type
     * that were written with {@link #writeTypedList} at the
     * current dataPosition().  The list <em>must</em> have
     * previously been written via {@link #writeTypedList} with the same object
     * type.
     *
     * @return A newly created ArrayList containing objects with the same data
     *         as those that were previously written.
     *
     * @see #writeTypedList
     */
    public final <T> void readTypedList(@NonNull List<T> list, @NonNull Parcelable.Creator<T> c) {
        throw new RuntimeException("Should implement with J2OBJC.");
    }

    public final void readMap(@NonNull Map outVal, @Nullable ClassLoader loader) {
        throw new RuntimeException("Should implement with J2OBJC");
    }


    public final void writeMap(@Nullable Map val) {
        throw new RuntimeException("Should implement with J2OBJC");
    }



    @Nullable
    public final Serializable readSerializable() {
        throw new RuntimeException("Should implement with J2OBJC");
    }

    public final void writeSerializable(@Nullable Serializable s) {
        throw new RuntimeException("Should implement with J2OBJC");
    }


    @Nullable
    public final HashMap readHashMap(@Nullable ClassLoader loader)
    {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        HashMap m = new HashMap(N);

        throw new RuntimeException("Should implement with J2OBJC");
        // readMapInternal(m, N, loader);
        // return m;
    }

}
