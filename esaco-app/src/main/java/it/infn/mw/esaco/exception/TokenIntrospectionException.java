/**
 * // SPDX-FileCopyrightText: 2026 Istituto Nazionale di Fisica Nucleare
 * //
 * // SPDX-License-Identifier: EUPL-1.2
 */
package it.infn.mw.esaco.exception;

public class TokenIntrospectionException extends IllegalArgumentException {

  private static final long serialVersionUID = 1L;

  public TokenIntrospectionException(String message) {

    super(message);
  }

  public TokenIntrospectionException(String message, Throwable cause) {

    super(message, cause);
  }
}
