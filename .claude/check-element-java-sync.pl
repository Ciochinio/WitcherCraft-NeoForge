use strict; use warnings;
my $BS = chr(92);
my %locked = map { $_ => 1 } qw(CatVision QuenBroke QuenActiveTick WitcherHud);
sub c2s { my $s = shift; $s =~ s/([a-z0-9])([A-Z])/$1_$2/g; uc $s }
my @rows; my $checked = 0;
for my $ef (sort glob("elements/*.mod.json")) {
    my ($n) = $ef =~ m{elements/(.+)\.mod\.json$};
    next if $locked{$n};
    my $jf = "src/main/java/net/redboltmedia/witchercraft/procedures/${n}Procedure.java";
    next unless -e $jf;
    open my $E,'<',$ef or next; local $/; my $x=<$E>; close $E;
    next unless $x =~ /procedurexml/;
    $x =~ s/\Q${BS}u003c\E/</gi; $x =~ s/\Q${BS}u003e\E/>/gi; $x =~ s/\Q${BS}"\E/"/g;
    open my $J,'<',$jf or next; my $j=<$J>; close $J;
    $checked++;
    my (%exp,%got);
    $exp{"var:$1"}++ while $x =~ /global:(witchercraft[A-Za-z0-9]+)</g;
    $got{"var:$1"}++ while $j =~ /\.(witchercraft[A-Za-z0-9]+)\b/g;
    while ($x =~ /<block type="entity_(?:add|remove)_modifier"><field name="name">([a-z0-9_]+)</g) { $exp{"mod:$1"}++ }
    # java side: only Identifiers actually used as attribute-modifier ids
    $got{"mod:$1"}++ while $j =~ /removeModifier\(Identifier\.parse\("witchercraft:([a-z0-9_]+)"\)/g;
    $got{"mod:$1"}++ while $j =~ /new AttributeModifier\(Identifier\.parse\("witchercraft:([a-z0-9_]+)"\)/g;
    while ($x =~ /<field name="attribute">CUSTOM:([A-Za-z0-9]+)</g) { $exp{"attr:".c2s($1)}++ }
    $got{"attr:$1"}++ while $j =~ /WitchercraftModAttributes\.([A-Z0-9_]+)/g;
    my @jo = grep { !exists $exp{$_} } sort keys %got;
    my @eo = grep { !exists $got{$_} } sort keys %exp;
    push @rows,[$n,\@jo,\@eo] if @jo || @eo;
}
printf "checked %d unlocked procedures\n\n", $checked;
if (!@rows) { print "No divergence detected.\n"; exit }
for my $r (@rows) {
    my ($n,$jo,$eo)=@$r;
    printf "%-24s ONLY-IN-JAVA:   %s\n", $n, join(", ",@$jo) if @$jo;
    printf "%-24s ONLY-IN-BLOCKS: %s\n", (@$jo?"":$n), join(", ",@$eo) if @$eo;
}
printf "\n%d procedure(s) diverge\n", scalar @rows;
