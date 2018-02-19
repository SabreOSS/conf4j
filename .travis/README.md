<!--
  MIT License

  Copyright 2017-2018 Sabre GLBL Inc.

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in all
  copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  SOFTWARE.
 -->
 
# Continuous Integration Environment setup 
 
## Prerequisites

### Generation & exporting GPG Keys

* generate GPG Keys used to sing JARs:
    
    `$ gpg --gen-key`
    
    ```
    gpg (GnuPG) X.Y.Z; Copyright (C) 2015 Free Software Foundation, Inc.
    This is free software: you are free to change and redistribute it.
    There is NO WARRANTY, to the extent permitted by law.
    
    Please select what kind of key you want:
       (1) RSA and RSA (default)
       (2) DSA and Elgamal
       (3) DSA (sign only)
       (4) RSA (sign only)
    Your selection? 1
    RSA keys may be between 1024 and 4096 bits long.
    What keysize do you want? (2048) 4096
    Requested keysize is 4096 bits
    Please specify how long the key should be valid.
             0 = key does not expire
          <n>  = key expires in n days
          <n>w = key expires in n weeks
          <n>m = key expires in n months
          <n>y = key expires in n years
    Key is valid for? (0) 0
    Key does not expire at all
    Is this correct? (y/N) y
    
    You need a user ID to identify your key; the software constructs the user ID
    from the Real Name, Comment and Email Address in this form:
        "Heinrich Heine (Der Dichter) <heinrichh@duesseldorf.de>"
    
    Real name: Sabre GLBL Inc.
    Email address: conf4j.oss@sabre.com
    Comment: Release Signing Key
    You selected this USER-ID:
        "Sabre GLBL Inc. (Release Signing Key) <conf4j.oss@sabre.com>"
    
    Change (N)ame, (C)omment, (E)mail or (O)kay/(Q)uit? O
    You need a Passphrase to protect your secret key.
    
    We need to generate a lot of random bytes. It is a good idea to perform
    some other action (type on the keyboard, move the mouse, utilize the
    disks) during the prime generation; this gives the random number
    generator a better chance to gain enough entropy.
    .............+++++
    .+++++
    We need to generate a lot of random bytes. It is a good idea to perform
    some other action (type on the keyboard, move the mouse, utilize the
    disks) during the prime generation; this gives the random number
    generator a better chance to gain enough entropy.
    +++++
    .+++++
    gpg: key 01234567 marked as ultimately trusted
    public and secret key created and signed.
    
    gpg: checking the trustdb
    gpg: 3 marginal(s) needed, 1 complete(s) needed, PGP trust model
    gpg: depth: 0  valid:   2  signed:   0  trust: 0-, 0q, 0n, 0m, 0f, 2u
    pub   4096R/01234567 2017-12-06
          Key fingerprint = 0123 0123 0123 0123 0123  0123 0123 0123 0123 0123
    uid                  Sabre GLBL Inc. (Release Signing Key) <conf4j.oss@sabre.com>
    sub   4096R/01234567 2017-12-06
    ```

    Please enter the passphrase for the keys.

* export GPG secret keys to **gpg_secret_keys.b64** file:
    
    `$ gpg -a --export-secret-keys conf4j.oss@sabre.com | base64 -w 0 > gpg_secret_keys.b64`

* export GPG ownertrust to **gpg_ownertrust.b64** file:

    `$ gpg --export-ownertrust | base64 -w 0 > gpg_ownertrust.b64`
    
* distribute GPG Public Key to publicly accessible key server

    `$ gpg --keyserver hkp://pgp.mit.edu --send-keys conf4j.oss@sabre.com`

### Generating SSH Keys

* generate SSH Key

    `$ ssh-keygen -t rsa -b 4096 -C "conf4j.oss@sabre.com"`
    
    Please save the certificate to the `id_rsa` file. In addition, public key is saved to `id_rsa.pub`. 
    
* convert certificate to base64 encoded `id_rsa.b64`:
    
    `$ cat id_rsa | base64 -w 0 > id_rsa.b64` 

## GitHub setup

* log in to the [GitHub](https://github.com) as `conf4j-ci` user 
* open the [Keys](https://github.com/settings/keys) section
  and add **SSH Public Key** there (content of `id_rsa.pub` file) 


NOTE: `conf4j-ci` user has to be allowed to write to [SabreOSS/conf4j](https://github.com/SabreOSS/conf4j) repository. 
 
## Travis CI setup

* log in to the GitHub
* open Travis CI [settings](https://travis-ci.org/SabreOSS/conf4j/settings) page for SabreOSS/conf4j 
  project and create environment variables required by [build.sh](build.sh) script (see the validation() function there).

  
  



