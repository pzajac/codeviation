#!/usr/bin/perl
$name = "/etc/passwd";
open(DATA,"$name")
   or die "Invalid file";
while ($line = <DATA> ) {
   chomp($line);
   print "run CVS_TAG $line";
   system("echo rm -rf *");
   system("echo cvs co -r $line standard_nowww");
   system("echo ant -f nbbuild/build.xml build-nozip")
}
close(DATA);   
