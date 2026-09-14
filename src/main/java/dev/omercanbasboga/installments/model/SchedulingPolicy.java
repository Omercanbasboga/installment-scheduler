package dev.omercanbasboga.installments.model;

public enum SchedulingPolicy {

    EQUAL {
        @Override
        public int[] weights(int installmentCount) {
            int[] w = new int[installmentCount];
            java.util.Arrays.fill(w, 1);
            return w;
        }
    },

    FRONT_LOADED {
        @Override
        public int[] weights(int installmentCount) {
            int[] w = new int[installmentCount];
            java.util.Arrays.fill(w, 1);
            w[0] = 2;
            return w;
        }
    };

    public abstract int[] weights(int installmentCount);
}
