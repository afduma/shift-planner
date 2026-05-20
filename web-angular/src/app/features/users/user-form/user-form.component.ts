import { Component, effect, inject, input, output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { SystemRole, UpdateUserRequest, User } from '../../../core/models/user.model';

const SYSTEM_ROLES: SystemRole[] = ['ADMIN', 'MANAGER', 'EMPLOYEE'];

@Component({
  selector: 'app-user-form',
  imports: [ReactiveFormsModule],
  templateUrl: './user-form.component.html',
  styleUrl: './user-form.component.scss',
})
export class UserFormComponent {
  private readonly formBuilder = inject(FormBuilder);

  readonly title = input.required<string>();
  readonly description = input<string>('');
  readonly submitLabel = input<string>('Save user');
  readonly initialValue = input<User | null>(null);
  readonly emailReadonly = input<boolean>(false);
  readonly isSubmitting = input<boolean>(false);
  readonly errorMessage = input<string>('');
  readonly submitted = output<UpdateUserRequest>();

  protected readonly systemRoles = SYSTEM_ROLES;
  protected readonly form = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
    firstName: ['', [Validators.required, Validators.maxLength(100)]],
    lastName: ['', [Validators.required, Validators.maxLength(100)]],
    active: [true],
    systemRole: ['EMPLOYEE' as SystemRole, Validators.required],
  });

  constructor() {
    effect(() => {
      const value = this.initialValue();
      if (value) {
        this.form.reset({
          email: value.email,
          firstName: value.firstName,
          lastName: value.lastName,
          active: value.active,
          systemRole: value.systemRole,
        });
      }
    });

    effect(() => {
      const emailControl = this.form.controls.email;

      if (this.emailReadonly()) {
        emailControl.disable({ emitEvent: false });
      } else {
        emailControl.enable({ emitEvent: false });
      }
    });
  }

  protected onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { firstName, lastName, active, systemRole } = this.form.getRawValue();
    this.submitted.emit({ firstName, lastName, active, systemRole });
  }
}
