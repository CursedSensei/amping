from django.db import connection
from django.shortcuts import redirect, render
from django.views import View

# Create your views here.
class RegisterView(View): # Registration page
    template_name = 'patientregister.html'

    def get(self, request):
        return render(request, self.template_name)

    def post(self, request):
        # get everything
        firstname = request.POST.get('firstname')
        lastname = request.POST.get('lastname')
        email = request.POST.get('email')
        contact = request.POST.get('contact')
        healthcare_provider_id = request.POST.get('healthcare_provider')
        birthdate = request.POST.get('birthdate')
        age = request.POST.get('age')

        try:
            with connection.cursor() as cursor:
                cursor.callproc('RegisterUser', [
                    email, firstname, lastname, contact, healthcare_provider_id, birthdate, age
                ])
                result = cursor.fetchall()

                if result:
                    db_message = result[0][0]
                    if db_message == 'Registration successful':
                        return redirect('User:LoginView')
                    else:
                        return render(request, self.template_name, {'message': db_message})

        except Exception as e:
            print(f"Patient Register Error: {e}")
            return render(request, self.template_name, {'message': 'System Error'})