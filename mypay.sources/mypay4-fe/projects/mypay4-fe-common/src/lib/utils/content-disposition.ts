/*
 *     MyPay - Payment portal of Regione Veneto.
 *     Copyright (C) 2022  Regione Veneto
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
export class ContentDispositionUtil{

  /**
   * RegExp for various RFC 6266 grammar
   *
   * disposition-type = "inline" | "attachment" | disp-ext-type
   * disp-ext-type    = token
   * disposition-parm = filename-parm | disp-ext-parm
   * filename-parm    = "filename" "=" value
   *                  | "filename*" "=" ext-value
   * disp-ext-parm    = token "=" value
   *                  | ext-token "=" ext-value
   * ext-token        = <the characters in token, followed by "*">
   * @private
   */

  private static DISPOSITION_TYPE_REGEXP = /^([!#$%&'*+.0-9A-Z^_`a-z|~-]+)[\x09\x20]*(?:$|;)/ // eslint-disable-line no-control-regex

  /**
   * RegExp for various RFC 2616 grammar
   *
   * parameter     = token "=" ( token | quoted-string )
   * token         = 1*<any CHAR except CTLs or separators>
   * separators    = "(" | ")" | "<" | ">" | "@"
   *               | "," | ";" | ":" | "\" | <">
   *               | "/" | "[" | "]" | "?" | "="
   *               | "{" | "}" | SP | HT
   * quoted-string = ( <"> *(qdtext | quoted-pair ) <"> )
   * qdtext        = <any TEXT except <">>
   * quoted-pair   = "\" CHAR
   * CHAR          = <any US-ASCII character (octets 0 - 127)>
   * TEXT          = <any OCTET except CTLs, but including LWS>
   * LWS           = [CRLF] 1*( SP | HT )
   * CRLF          = CR LF
   * CR            = <US-ASCII CR, carriage return (13)>
   * LF            = <US-ASCII LF, linefeed (10)>
   * SP            = <US-ASCII SP, space (32)>
   * HT            = <US-ASCII HT, horizontal-tab (9)>
   * CTL           = <any US-ASCII control character (octets 0 - 31) and DEL (127)>
   * OCTET         = <any 8-bit sequence of data>
   * @private
   */

  private static PARAM_REGEXP = /;[\x09\x20]*([!#$%&'*+.0-9A-Z^_`a-z|~-]+)[\x09\x20]*=[\x09\x20]*("(?:[\x20!\x23-\x5b\x5d-\x7e\x80-\xff]|\\[\x20-\x7e])*"|[!#$%&'*+.0-9A-Z^_`a-z|~-]+)[\x09\x20]*/g // eslint-disable-line no-control-regex

  /**
   * RegExp to match quoted-pair in RFC 2616
   *
   * quoted-pair = "\" CHAR
   * CHAR        = <any US-ASCII character (octets 0 - 127)>
   * @private
   */

  private static QESC_REGEXP = /\\([\u0000-\u007f])/g // eslint-disable-line no-control-regex

  /**
   * RegExp for various RFC 5987 grammar
   *
   * ext-value     = charset  "'" [ language ] "'" value-chars
   * charset       = "UTF-8" / "ISO-8859-1" / mime-charset
   * mime-charset  = 1*mime-charsetc
   * mime-charsetc = ALPHA / DIGIT
   *               / "!" / "#" / "$" / "%" / "&"
   *               / "+" / "-" / "^" / "_" / "`"
   *               / "{" / "}" / "~"
   * language      = ( 2*3ALPHA [ extlang ] )
   *               / 4ALPHA
   *               / 5*8ALPHA
   * extlang       = *3( "-" 3ALPHA )
   * value-chars   = *( pct-encoded / attr-char )
   * pct-encoded   = "%" HEXDIG HEXDIG
   * attr-char     = ALPHA / DIGIT
   *               / "!" / "#" / "$" / "&" / "+" / "-" / "."
   *               / "^" / "_" / "`" / "|" / "~"
   * @private
   */

  private static EXT_VALUE_REGEXP = /^([A-Za-z0-9!#$%&+\-^_`{}~]+)'(?:[A-Za-z]{2,3}(?:-[A-Za-z]{3}){0,3}|[A-Za-z]{4,8}|)'((?:%[0-9A-Fa-f]{2}|[A-Za-z0-9!#$&+.^_`|~-])+)$/

  /**
   * RegExp to match percent encoding escape.
   * @private
   */

  private static HEX_ESCAPE_REGEXP = /%[0-9A-Fa-f]{2}/
  private static HEX_ESCAPE_REPLACE_REGEXP = /%([0-9A-Fa-f]{2})/g

  /**
   * RegExp to match non-latin1 characters.
   * @private
   */

  private static NON_LATIN1_REGEXP = /[^\x20-\x7e\xa0-\xff]/g



  /**
   * Parse Content-Disposition header string.
   *
   * @param {string} string
   * @return {object}
   * @public
   */

  static parse (string): ContentDisposition {
    try{
      if (!string || typeof string !== 'string') {
        throw new TypeError('argument string is required')
      }

      var match = ContentDispositionUtil.DISPOSITION_TYPE_REGEXP.exec(string)

      if (!match) {
        throw new TypeError('invalid type format')
      }

      // normalize type
      var index = match[0].length
      var type = match[1].toLowerCase()

      var key
      var names = []
      var params = {}
      var value

      // calculate index to start at
      index = ContentDispositionUtil.PARAM_REGEXP.lastIndex = match[0].substr(-1) === ';'
        ? index - 1
        : index

      // match parameters
      while ((match = ContentDispositionUtil.PARAM_REGEXP.exec(string))) {
        if (match.index !== index) {
          throw new TypeError('invalid parameter format')
        }

        index += match[0].length
        key = match[1].toLowerCase()
        value = match[2]

        if (names.indexOf(key) !== -1) {
          throw new TypeError('invalid duplicate parameter')
        }

        names.push(key)

        if (key.indexOf('*') + 1 === key.length) {
          // decode extended value
          key = key.slice(0, -1)
          value = this.decodefield(value)

          // overwrite existing value
          params[key] = value
          continue
        }

        if (typeof params[key] === 'string') {
          continue
        }

        if (value[0] === '"') {
          // remove quotes and escapes
          value = value
            .substr(1, value.length - 2)
            .replace(ContentDispositionUtil.QESC_REGEXP, '$1')
        }

        params[key] = value
      }

      if (index !== -1 && index !== string.length) {
        throw new TypeError('invalid parameter format')
      }

      return new ContentDisposition(type, params)
    }catch(error){
      console.error(error);
      return null;
    }
  }

  /**
   * Decode a RFC 5987 field value (gracefully).
   *
   * @param {string} str
   * @return {string}
   * @private
   */

  private static decodefield (str) {
    var match = ContentDispositionUtil.EXT_VALUE_REGEXP.exec(str)

    if (!match) {
      throw new TypeError('invalid extended field value')
    }

    var charset = match[1].toLowerCase()
    var encoded = match[2]
    var value

    // to binary string
    var binary = encoded.replace(ContentDispositionUtil.HEX_ESCAPE_REPLACE_REGEXP, ContentDispositionUtil.pdecode)

    switch (charset) {
      case 'iso-8859-1':
        value = ContentDispositionUtil.getlatin1(binary)
        break
      case 'utf-8':
        value = binary //SafeBuffer.Buffer.from(binary, 'binary').toString('utf8')
        break
      default:
        throw new TypeError('unsupported charset in extended field')
    }

    return value
  }

  /**
   * Percent decode a single character.
   *
   * @param {string} str
   * @param {string} hex
   * @return {string}
   * @private
   */

   private static pdecode (str, hex) {
    return String.fromCharCode(parseInt(hex, 16))
  }

  /**
   * Get ISO-8859-1 version of string.
   *
   * @param {string} val
   * @return {string}
   * @private
   */

   private static getlatin1 (val) {
    // simple Unicode -> ISO-8859-1 transformation
    return String(val).replace(ContentDispositionUtil.NON_LATIN1_REGEXP, '?')
  }

}

/**
* Class for parsed Content-Disposition header for v8 optimization
*
* @public
* @param {string} type
* @param {object} parameters
* @constructor
*/

export class ContentDisposition {
  public type: string;
  public parameters: {};
  constructor(type: string, parameters: {}){
    this.type = type;
    this.parameters = parameters;
  }
}
