/**
 * // SPDX-FileCopyrightText: 2026 Istituto Nazionale di Fisica Nucleare
 * //
 * // SPDX-License-Identifier: EUPL-1.2
 */
package it.infn.mw.esaco.exception;

public class SSLContextInitializationError extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 4273277723565905211L;

  public SSLContextInitializationError(String message, Throwable cause) {
    super(message, cause);
  }

}
