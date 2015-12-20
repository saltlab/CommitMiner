%% %%%%%%%%%%%%%%%%%%%%
%% Define the possible predicates.
%% %%%%%%%%%%%%%%%%%%%%

% destination_protected_dereference := protected a dereferenced variable by adding a branch condition
% arg1 := the function identifier
% arg2 := the name of the identifier (variable, field, or function return value) being dereferenced.
destination_protected_dereference(_,_) :- fail.

% source_unprotected_dereference := unprotected a dereferenced variable by removing a branch condition
% arg1 := the function identifier
% arg2 := the name of the identifier (variable, field, or function return value) being dereferenced.
source_unprotected_dereference(_,_) :- fail.

%% %%%%%%%%%%%%%%%%%%%%
%% Define the rules.
%% %%%%%%%%%%%%%%%%%%%%

% Alert for when a variable is protected by a conditional
alert_undefined_protected(X, Y) :- destination_protected_dereference(X, Y), \+ source_unprotected_dereference(X, Y).

%% %%%%%%%%%%%%%%%%%%%%
%% Create some facts for testing.
%% %%%%%%%%%%%%%%%%%%%%

source_unprotected_dereference('script.~anonymous~', 'error').
destination_protected_dereference('script.~anonymous~', 'error').

destination_protected_dereference('script.getName', 'error').

destination_protected_dereference('script.getEMail', 'error').

%% %%%%%%%%%%%%%%%%%%%%
%% Query the rules to find instances.
%% %%%%%%%%%%%%%%%%%%%%

?- findall([X,Y], alert_undefined_protected(X,Y),Z).
