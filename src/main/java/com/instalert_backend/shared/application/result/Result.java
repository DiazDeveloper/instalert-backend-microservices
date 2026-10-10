package com.instalert_backend.shared.application.result;

import java.util.Optional;
import java.util.function.Function;

public sealed interface Result<T, E> permits Result.Success, Result.Failure {

    static <T, E> Result<T, E> success(T value) {
        return new Success<>(value);
    }

    static <T, E> Result<T, E> failure(E error) {
        return new Failure<>(error);
    }

    default boolean isSuccess() {
        return this instanceof Success<?, ?>;
    }

    default boolean isFailure() {
        return this instanceof Failure<?, ?>;
    }

    default Optional<T> success() {
        if (this instanceof Success<?, ?> success) {
            @SuppressWarnings("unchecked")
            T value = ((Success<T, E>) success).value();
            return Optional.of(value);
        }
        return Optional.empty();
    }

    default Optional<E> failure() {
        if (this instanceof Failure<?, ?> failure) {
            @SuppressWarnings("unchecked")
            E error = ((Failure<T, E>) failure).error();
            return Optional.of(error);
        }
        return Optional.empty();
    }

    default <R> R fold(Function<? super T, ? extends R> onSuccess, Function<? super E, ? extends R> onFailure) {
        if (this instanceof Success<?, ?> success) {
            @SuppressWarnings("unchecked")
            T value = ((Success<T, E>) success).value();
            return onSuccess.apply(value);
        }

        @SuppressWarnings("unchecked")
        E error = ((Failure<T, E>) this).error();
        return onFailure.apply(error);
    }

    record Success<T, E>(T value) implements Result<T, E> {
    }

    record Failure<T, E>(E error) implements Result<T, E> {
    }
}